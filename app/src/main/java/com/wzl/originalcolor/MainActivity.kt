package com.wzl.originalcolor

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.chip.Chip
import com.wzl.originalcolor.adapter.ColorAdapter
import com.wzl.originalcolor.databinding.ActivityMainBinding
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorItemDecoration
import com.wzl.originalcolor.utils.OriginalColorUtils
import com.wzl.originalcolor.utils.PxUtils
import com.wzl.originalcolor.utils.VibratorUtils
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ColorAdapter
    private var colorList = listOf<OriginalColor>()
    private var gridCount: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            super.onCreate(savedInstanceState)
        } else {
            super.onCreate(null)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVibration()
        colorList = OriginalColorUtils.getAllColors(this)
        adapter = ColorAdapter()
        adapter.setItemAnimation(BaseQuickAdapter.AnimationType.AlphaIn)
        updateColorList(colorList)
        binding.recyclerView.apply {
            gridCount = when (requestedOrientation) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED -> {
                    val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                    val screenHeight = Resources.getSystem().displayMetrics.heightPixels
                    if (screenWidth > screenHeight) 3 else 1
                }
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> 3
                else -> 1
            }
            layoutManager = GridLayoutManager(this@MainActivity, gridCount)
            addItemDecoration(ColorItemDecoration(this@MainActivity, gridCount))
        }
        binding.recyclerView.adapter = adapter
        adapter.addOnItemChildClickListener(R.id.colorCardView) { adapter, view, position ->
            val originalColor = adapter.getItem(position) ?: return@addOnItemChildClickListener
            val modalBottomSheet = ModalBottomSheet(originalColor)
            modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.fabSearch.let { fabSearch ->
                    if (dy > 30 && fabSearch.isShown) {
                        fabSearch.hide()
                    } else if (dy < -30 && !fabSearch.isShown) {
                        fabSearch.show()
                    }
                }
            }
        })

        binding.searchInnerView.colorChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chipTag = group.findViewById<Chip>(checkedId).text.toString()
            updateColorList(getColorListByTag(chipTag))
            binding.colorSearchView.hide()
        }

        binding.colorSearchView.let { searchView ->
            searchView.editText.setOnEditorActionListener { v, actionId, event ->
                val searchText = searchView.text.toString()
                if (searchText.isNotEmpty()) {
                    updateColorList(searchColorByKeyword(searchText))
                }
                searchView.hide()
                false
            }
        }

        binding.fabSearch.setOnClickListener {
            binding.colorSearchView.show()
        }

        binding.topAppBar.setNavigationOnClickListener {
            VibratorUtils.vibrate(this)
            binding.recyclerView.apply {
                (layoutManager as GridLayoutManager).scrollToPositionWithOffset(
                    Random.nextInt(0, this@MainActivity.adapter.itemCount - 1), PxUtils.dp2px(context, 16)
                )
            }
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
//                    MaterialAlertDialogBuilder(this)
//                        .setTitle(R.string.menu_info)
//                        .setMessage(R.string.color_data_copyright)
//                        .show()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    // 其他：淡肉色 棕色 粉色 褐色 赭 醉瓜肉 淡咖啡 金驼
    private fun getColorListByTag(tag: String): List<OriginalColor> {
        if (tag == getString(R.string.chip_full)) {
            return colorList
        }
        return if (tag != getString(R.string.chip_other)) {
            colorList.filter { it.NAME.last().toString() == tag }
        } else {
            colorList.filter {
                !COLORS.contains(it.NAME.last().toString())
            }
        }
    }

    private fun searchColorByKeyword(keyword: String): List<OriginalColor> {
        return colorList.filter { it.NAME.contains(keyword) }
    }

    private fun updateColorList(list: List<OriginalColor>) {
        adapter.submitList(list)
    }

    private fun initVibration() {
        val sp = getSharedPreferences("settings", Context.MODE_PRIVATE)
        VibratorUtils.updateVibration(sp.getBoolean("vibration", true))
    }
}

val COLORS = arrayOf("白", "灰", "红", "橙", "黄", "绿", "青", "蓝", "紫")