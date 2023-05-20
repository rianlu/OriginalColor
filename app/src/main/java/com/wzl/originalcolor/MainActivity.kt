package com.wzl.originalcolor

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.search.SearchView
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
        super.onCreate(null)

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

        binding.searchInnerView.colorChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d("checkedIds: ", checkedIds.toIntArray().arrayToString())
            // 选择标签自动清除搜索框文本
            binding.colorSearchView.editText.text.clear()
            val chipTag = group.findViewById<Chip>(checkedIds.first()).text.toString()
            updateColorList(OriginalColorUtils.getColorListByTag(this, chipTag))
            binding.colorSearchView.hide()
        }

        binding.colorSearchView.let { searchView ->
            searchView.editText.setOnEditorActionListener { v, actionId, event ->
                val keyword = searchView.text.toString()
                if (keyword.isNotEmpty()) {
                    updateColorList(OriginalColorUtils.searchColorByKeyword(this, keyword))
                } else {
                    binding.searchInnerView.colorChipGroup.let { group ->
                        val chipTag = group.findViewById<Chip>(group.checkedChipId).text.toString()
                        updateColorList(OriginalColorUtils.getColorListByTag(this, chipTag))
                    }
                }
                searchView.hide()
                false
            }
            searchView.addTransitionListener { view, previousState, newState ->
                if (binding.searchInnerView.colorChipGroup.checkedChipId == R.id.chipFull) {
                    return@addTransitionListener
                }
            }
        }

        binding.fabSearch.setOnClickListener {
            binding.colorSearchView.show()
        }

        binding.fabSearch.setOnLongClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
            true
        }

        binding.topAppBar.setNavigationOnClickListener {
            VibratorUtils.vibrate(this)
            binding.recyclerView.apply {
                (layoutManager as GridLayoutManager).scrollToPositionWithOffset(
                    Random.nextInt(0, this@MainActivity.adapter.itemCount - 1),
                    PxUtils.dp2px(context, 16)
                )
            }
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    startActivity(
                        Intent(this, SettingsActivity::class.java),
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                    )
                    true
                }

                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (binding.colorSearchView.currentTransitionState == SearchView.TransitionState.SHOWN) {
            binding.colorSearchView.hide()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateColorList(list: List<OriginalColor>) {
        binding.emptyDataView.isVisible = list.isEmpty()
        adapter.submitList(list)
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
}

val COLORS = arrayOf("白", "灰", "红", "橙", "黄", "绿", "青", "蓝", "紫")