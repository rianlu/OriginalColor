package com.wzl.originalcolor.viewmodel

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * @Author lu
 * @Date 2023/7/22 16:13
 * @ClassName: ColorViewModel
 * @Description:
 */
class ColorViewModel : ViewModel() {

    private var colorList: List<OriginalColor> = mutableListOf()

    private var _flowLst = MutableStateFlow<List<OriginalColor>>(emptyList())
    var flowList = _flowLst
        private set

    private val _isLoading = MutableStateFlow(true)
    var isLoading = _isLoading
        private set

    fun initData(context: Context) {
        if (_flowLst.value.isNotEmpty()) {
            return
        }
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val list = ColorData.initData(context)
            colorList = list
            _flowLst.value = list
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 500) {
                kotlinx.coroutines.delay(500 - elapsedTime)
            }
            _isLoading.value = false
        }
    }

    // 其他：淡肉色 棕色 粉色 褐色 赭 醉瓜肉 淡咖啡 金驼
    // 筛选
    fun filterByTag(context: Context, tag: String) {
        val filterTag = COLOR_MAPS[tag] ?: tag
        val allText = context.getString(R.string.chip_full)
        val otherText = context.getString(R.string.chip_other)
        
        val fullList = colorList.ifEmpty { _flowLst.value }
        
        _flowLst.value = when (filterTag) {
            allText -> fullList
            otherText -> fullList.filter { !COLOR_TAGS.contains(it.NAME.last().toString()) }
            else -> fullList.filter { it.NAME.last().toString() == filterTag }
        }
    }

    // 搜索
    fun searchByKeyword(keyword: String) {
        val fullList = colorList.ifEmpty { _flowLst.value }
        _flowLst.value = fullList.filter {
            it.NAME.contains(keyword) || it.pinyin.contains(keyword)
        }
    }
}

val COLOR_MAPS = mapOf(
     "All" to "全部",
     "White" to "白",
     "Grey" to "灰",
     "Red" to "红",
     "Orange" to "橙",
     "Yellow" to "黄",
     "Green" to "绿",
     "Cyan" to "青",
     "Blue" to "蓝",
     "Purple" to "紫",
     "Others" to "其他"
)
val COLOR_TAGS = arrayOf("白", "灰", "红", "橙", "黄", "绿", "青", "蓝", "紫")