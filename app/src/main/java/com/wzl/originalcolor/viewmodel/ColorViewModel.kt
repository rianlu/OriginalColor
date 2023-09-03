package com.wzl.originalcolor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorData
import kotlinx.coroutines.flow.MutableStateFlow

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

    fun initData(context: Context): List<OriginalColor> {
        if (colorList.isNotEmpty()) {
            return colorList
        }
        colorList = ColorData.initData(context)
        _flowLst.value = colorList
        return colorList
    }

    // 其他：淡肉色 棕色 粉色 褐色 赭 醉瓜肉 淡咖啡 金驼
    // 筛选
    fun filterByTag(context: Context, tag: String) {
        _flowLst.value = if (tag == context.getString(R.string.chip_full)) {
            colorList
        } else if (tag != context.getString(R.string.chip_other)) {
            colorList.filter { it.NAME.last().toString() == tag }
        } else {
            colorList.filter {
                !COLOR_TAGS.contains(it.NAME.last().toString())
            }
        }
    }

    // 搜索
    fun searchByKeyword(keyword: String) {
        _flowLst.value = colorList.filter { it.NAME.contains(keyword) }
    }
}

val COLOR_TAGS = arrayOf("白", "灰", "红", "橙", "黄", "绿", "青", "蓝", "紫")