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
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.search.SearchView
import com.wzl.originalcolor.adapter.ColorAdapter
import com.wzl.originalcolor.databinding.ActivityMainBinding
import com.wzl.originalcolor.utils.ColorExtensions.setAlpha
import com.wzl.originalcolor.utils.ColorItemDecoration
import com.wzl.originalcolor.utils.PHONE_GRID_COUNT
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.ScreenUtils
import com.wzl.originalcolor.utils.TABLET_GRID_COUNT
import com.wzl.originalcolor.utils.VibratorUtils
import com.wzl.originalcolor.viewmodel.ColorViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
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

        val themeSp = getSharedPreferences(
            Config.SP_GLOBAL_THEME_COLOR, Context.MODE_PRIVATE)
        val hex = themeSp.getString(Config.SP_PARAM_THEME_COLOR, null)
        hex?.let { updateGlobalThemeColor(it) }

        if (ScreenUtils.isPad(this@MainActivity)) {
            gridCount = TABLET_GRID_COUNT
        }
        initVibration()
        adapter = ColorAdapter().also {
            it.setItemAnimation(BaseQuickAdapter.AnimationType.AlphaIn)
            it.addOnItemChildClickListener(R.id.colorBackground) { adapter, view, position ->
                val originalColor = adapter.getItem(position) ?: return@addOnItemChildClickListener
                val modalBottomSheet = ModalBottomSheet(originalColor)
                modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
            }
            it.addOnItemChildLongClickListener(R.id.colorBackground) {
                    adapter, view, position ->
                adapter.getItem(position)?.let { originColor ->
                    val themeColor = originColor.HEX
                    themeSp.edit().apply {
                        putString(Config.SP_PARAM_THEME_COLOR, themeColor)
                        apply()
                    }
                    VibratorUtils.vibrate(this)
                    updateGlobalThemeColor(themeColor)
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
        }
        binding.recyclerView.adapter = adapter
        lifecycleScope.launch {
            launch {
                colorViewModel.flowList.collect { list ->
                    binding.emptyListPlaceholder.isVisible = list.isEmpty()
                    adapter.submitList(list)
                    binding.recyclerView.isVisible = list.isNotEmpty()
                    binding.recyclerView.scrollToPositionWithOffset(
                        0, 16.dp(this@MainActivity)
                    )
                }
            }
            launch {
                fabSearchStateFlow.collect { state ->
                    binding.fabSearch.setImageIcon(
                        Icon.createWithResource(this@MainActivity,
                            if (state) {
                                R.drawable.ic_search
                            } else {
                                R.drawable.ic_top
                            }))
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
                if (newState == SearchView.TransitionState.SHOWING) {
                    binding.fabSearch
                        .animate().scaleX(0F).scaleY(0F)
                        .setDuration(200)
                } else if (newState == SearchView.TransitionState.HIDING) {
                    binding.fabSearch
                        .animate().scaleX(1F).scaleY(1F)
                        .setDuration(500)
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
                VibratorUtils.vibrate(this@MainActivity)
                val randomPosition = Random.nextInt(0, adapter.itemCount)
                binding.recyclerView.scrollToPositionWithOffset(
                    randomPosition, 16.dp(this@MainActivity)
                )
                if (randomPosition == 0) return@setNavigationOnClickListener
                if (randomPosition == 1 && ScreenUtils.isPad(this@MainActivity))
                    return@setNavigationOnClickListener
                fabSearchStateFlow.value = false
            }
            setOnMenuItemClickListener {menuItem ->
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
        // 刷新 Widget
        val widgetWorkRequest = PeriodicWorkRequestBuilder<WidgetWorker>(
            15L, TimeUnit.SECONDS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WIDGET_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, widgetWorkRequest
        )
    }

    override fun onBackPressed() {
        if (binding.colorSearchView.currentTransitionState == SearchView.TransitionState.SHOWN) {
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

    private fun initVibration() {
        val sp = getSharedPreferences("settings", Context.MODE_PRIVATE)
        VibratorUtils.updateVibration(sp.getBoolean("vibration", true))
    }

    private fun IntArray.arrayToString(): String {
        val builder = StringBuilder()
        this.forEachIndexed { index, i ->
            builder.append(i)
            if (index != this.size - 1) {
                builder.append(", ")
            }
        }
        return builder.toString()
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
    }

    private fun updateGlobalThemeColor(hex: String) {

        val themeColor = Color.parseColor(hex)
        // 标题
        binding.collapsingToolbarLayout.apply {
            // 折叠后的CollapsingToolbarLayout
            setContentScrimColor(themeColor.setAlpha(0.1F))
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
        binding.fabSearch.backgroundTintList = ColorStateList.valueOf(themeColor)

        // Search Chips
        binding.searchInnerView.colorChipGroup.apply {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_selected),
                intArrayOf(-android.R.attr.state_checked)
                )
            val colors = intArrayOf(
                themeColor, themeColor,
                themeColor.setAlpha(0.1F),
                themeColor.setAlpha(0.1F)
            )
            forEach {
                (it as Chip).chipBackgroundColor =
                    ColorStateList(states, colors)
            }
        }

        binding.colorSearchView.apply {
            backgroundTintList =
                ColorStateList.valueOf(themeColor)
        }

        // List Scrollbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.recyclerView.verticalScrollbarThumbDrawable?.setTint(themeColor)
        }
    }
}

const val WIDGET_WORKER_TAG = "widget_worker_tag"