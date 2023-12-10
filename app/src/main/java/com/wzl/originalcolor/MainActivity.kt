package com.wzl.originalcolor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.animation.CycleInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.search.CustomSearchView
import com.wzl.originalcolor.adapter.ColorAdapter
import com.wzl.originalcolor.databinding.ActivityMainBinding
import com.wzl.originalcolor.utils.ColorExtensions.brightness
import com.wzl.originalcolor.utils.ColorExtensions.isLight
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.ColorItemDecoration
import com.wzl.originalcolor.utils.PHONE_GRID_COUNT
import com.wzl.originalcolor.utils.ProtocolDialogUtil
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.ScreenUtil
import com.wzl.originalcolor.utils.SpUtil
import com.wzl.originalcolor.utils.SystemBarUtil
import com.wzl.originalcolor.utils.TABLET_GRID_COUNT
import com.wzl.originalcolor.utils.UiModeUtil
import com.wzl.originalcolor.utils.VibratorUtil
import com.wzl.originalcolor.utils.WorkManagerUtil
import com.wzl.originalcolor.viewmodel.ColorViewModel
import com.wzl.originalcolor.widget.ColorWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ColorAdapter
    private var gridCount: Int = PHONE_GRID_COUNT
    private val colorViewModel by viewModels<ColorViewModel>()

    // FAB State
    private val fabSearchStateFlow = MutableStateFlow(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 是否显示隐私弹窗
        ProtocolDialogUtil.show(this)

        val isPad = ScreenUtil.isPad(this@MainActivity)
        updateGlobalThemeColor(SpUtil.getLocalThemeColor(this), this)
        if (isPad) {
            gridCount = TABLET_GRID_COUNT
        }
        VibratorUtil.updateVibration(SpUtil.getVibrationState(this))

        adapter = ColorAdapter().also {
            it.setItemAnimation(BaseQuickAdapter.AnimationType.AlphaIn)
            it.addOnItemChildClickListener(R.id.originalColorCard) { adapter, view, position ->
                val originalColor = adapter.getItem(position) ?: return@addOnItemChildClickListener
                val modalBottomSheet = ModalBottomSheet(originalColor)
                val existedBottomSheet = supportFragmentManager.findFragmentByTag(ModalBottomSheet.TAG)
                if (existedBottomSheet != null && existedBottomSheet is ModalBottomSheet) {
                    existedBottomSheet.dismiss()
                }
                modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
            }
            it.addOnItemChildLongClickListener(R.id.colorBackground) { adapter, view, position ->
                adapter.getItem(position)?.let { originColor ->
                    val themeColor = originColor.HEX
                    SpUtil.saveLocalThemeColor(this, themeColor)
                    VibratorUtil.vibrate(this)
                    updateGlobalThemeColor(themeColor, this)
                    refreshWidget()
                }
                true
            }
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, gridCount)
            addItemDecoration(ColorItemDecoration(this@MainActivity, gridCount))
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    binding.fabSearch.let { fabSearch ->
                        if (dy > 30) {
                            fabSearchStateFlow.value = false
                        } else if (dy < -30) {
                            fabSearchStateFlow.value = true
                        }
                    }
                }
            })
            adapter = this@MainActivity.adapter
        }
        lifecycleScope.launch {
            launch {
                colorViewModel.flowList.collect { list ->
                    binding.emptyListPlaceholder.isVisible = list.isEmpty()
                    adapter.submitList(list)
                    binding.recyclerView.scrollToPositionWithOffset(
                        0, 16.dp(this@MainActivity)
                    )
                }
            }
            launch {
                fabSearchStateFlow.collect { state ->
                    binding.fabSearch.setImageIcon(
                        Icon.createWithResource(
                            this@MainActivity,
                            if (state) {
                                R.drawable.ic_search
                            } else {
                                R.drawable.ic_top
                            }
                        )
                    )
                }
            }
        }

        binding.searchInnerView.colorChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // 选择标签自动清除搜索框文本
            binding.colorSearchView.editText.text.clear()
            val chipTag = group.findViewById<Chip>(checkedIds.first()).text.toString()
            colorViewModel.filterByTag(this, chipTag)
            binding.colorSearchView.hide()
        }

        binding.colorSearchView.let { searchView ->
            searchView.editText.setOnEditorActionListener { v, actionId, event ->
                val keyword = searchView.text.toString()
                if (keyword.isNotEmpty()) {
                    colorViewModel.searchByKeyword(keyword)
                } else {
                    binding.searchInnerView.colorChipGroup.let { group ->
                        val chipTag = group.findViewById<Chip>(group.checkedChipId).text.toString()
                        colorViewModel.filterByTag(this, chipTag)
                    }
                }
                searchView.hide()
                false
            }
            searchView.addTransitionListener { view, previousState, newState ->
                val searchBackgroundViewColor =
                    searchView.generateBackgroundViewColor(
                        Color.parseColor(SpUtil.getLocalThemeColor(this))
                    )
                when (newState) {
                    CustomSearchView.TransitionState.SHOWN -> {
                        // 修改状态栏颜色
                        SystemBarUtil
                            .setStatusBarColor(this, searchBackgroundViewColor)
                    }

                    CustomSearchView.TransitionState.HIDDEN -> {
                    }

                    CustomSearchView.TransitionState.SHOWING -> {
                        binding.fabSearch
                            .animate().scaleX(0F).scaleY(0F)
                            .setDuration(200)
                            .start()
                    }

                    CustomSearchView.TransitionState.HIDING -> {
                        binding.fabSearch
                            .animate().scaleX(1F).scaleY(1F)
                            .setDuration(500)
                            .start()
                        // 恢复状态栏颜色
                        SystemBarUtil.setStatusBarColor(this, Color.TRANSPARENT)
                    }
                }
            }
        }

        binding.fabSearch.setOnClickListener {
            if (fabSearchStateFlow.value) {
                binding.colorSearchView.show()
            } else {
                binding.recyclerView.scrollToPositionWithOffset(0)
                fabSearchStateFlow.value = true
            }
        }

        binding.topAppBar.apply {
            setNavigationOnClickListener {
                if (adapter.itemCount == 0)
                    return@setNavigationOnClickListener
                it.animate().rotationBy(360F)
                    .setDuration(500)
                    .setInterpolator(CycleInterpolator(1F))
                    .start()
                VibratorUtil.vibrate(this@MainActivity)
                val randomPosition = Random.nextInt(0, adapter.itemCount)
                binding.recyclerView.scrollToPositionWithOffset(
                    randomPosition, 16.dp(this@MainActivity)
                )
                fabSearchStateFlow.value = randomPosition == 0 ||
                    (randomPosition == 1 && isPad)
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.settings -> {
                        startActivity(
                            Intent(this@MainActivity, SettingsActivity::class.java)
                        )
                        true
                    }

                    else -> false
                }
            }
        }
        colorViewModel.initData(this)

        // 每次打开刷新 Widget
        refreshWidget()

        // 开启定时刷新
        if (SpUtil.getWidgetRefreshState(this)) {
            WorkManagerUtil.startWork(this)
        }
    }

    override fun onBackPressed() {
        if (binding.colorSearchView.currentTransitionState == CustomSearchView.TransitionState.SHOWN) {
            binding.colorSearchView.hide()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.getSerializableExtra("widgetColor")?.let { widgetColor ->
            val position = colorViewModel.flowList.value.indexOf(widgetColor)
            binding.recyclerView.scrollToPositionWithOffset(
                position, 16.dp(this@MainActivity)
            )
        }
    }

    private fun refreshWidget() {
        sendBroadcast(Intent(this, ColorWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(this@MainActivity)
                .getAppWidgetIds(ComponentName(this@MainActivity, ColorWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        })
    }

    private fun RecyclerView.scrollToPositionWithOffset(position: Int, offset: Int = 0) {
        this.stopScroll()
        // 折叠标题栏，保持视觉统一
        if (position != 0) {
            binding.appBarLayout.setExpanded(false, true)
        }
        (layoutManager as GridLayoutManager)
            .scrollToPositionWithOffset(position, offset)
        this.translationY = 100F
        animate()
            .translationY(0F)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator(3F))
            .start()
    }

    private fun updateGlobalThemeColor(hex: String, context: Context) {

        val themeColor = Color.parseColor(hex)
        // 搜索背景色
        binding.colorSearchView.setBackgroundViewColor(themeColor)
        binding.colorSearchView.setDividerMargin(16.dp(context))
        binding.colorSearchView.setDividerBackgroundColor(themeColor)
        // 标题
        binding.collapsingToolbarLayout.apply {
            // 折叠后的CollapsingToolbarLayout
            setContentScrimColor(themeColor.setAlpha(0.2F))
            setExpandedTitleColor(themeColor)
            setCollapsedTitleTextColor(themeColor)
        }
        binding.topAppBar.apply {
            // Toolbar NavigationIcon
            setNavigationIconTint(themeColor)
            // 菜单——设置
            menu.getItem(0).icon?.setTint(themeColor)
        }
        // FAB
        binding.fabSearch.apply {
            backgroundTintList = ColorStateList.valueOf(themeColor)
            imageTintList = ColorStateList.valueOf(if (themeColor.isLight()) {
                themeColor.brightness(-0.5F)
            } else {
                themeColor.brightness(0.5F)
            })
        }

        // Search Chips
        binding.searchInnerView.colorChipGroup.apply {
            forEach {
                (it as Chip).updateThemeColor(themeColor)
            }
        }

        // List Scrollbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.recyclerView.verticalScrollbarThumbDrawable?.setTint(themeColor)
        }
    }

    private fun Chip.updateThemeColor(themeColor: Int) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_selected),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(
            themeColor, themeColor,
            themeColor.setAlpha(0.3F),
            themeColor.setAlpha(0.3F)
        )
        chipBackgroundColor =
            ColorStateList(states, colors)
        val dynamicCheckedColor =
            if (themeColor.isLight()) Color.BLACK
            else Color.WHITE
        val dynamicDefaultColors =
            if (UiModeUtil.isLightMode(context)) Color.BLACK
            else Color.WHITE
        val dynamicColors = intArrayOf(
            dynamicCheckedColor, dynamicCheckedColor,
            dynamicDefaultColors, dynamicDefaultColors
        )
        checkedIconTint = ColorStateList(states, dynamicColors)
        setTextColor(ColorStateList(states, dynamicColors))
    }
}