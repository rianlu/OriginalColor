package com.wzl.originalcolor.viewmodel

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorData
import kotlinx.coroutines.flow.MutableStateFlow
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
        val filterTag = if (COLOR_MAPS.containsValue(tag)) {
            tag
        } else {
            COLOR_MAPS.getValue(tag)
        }
        _flowLst.value = if (filterTag == context
            .getLocaleStringResource(R.string.chip_full)) {
            colorList
        } else if (filterTag != context
            .getLocaleStringResource(R.string.chip_other)) {
            colorList.filter { it.NAME.last().toString() == filterTag }
        } else {
            colorList.filter {
                !COLOR_TAGS.contains(it.NAME.last().toString())
            }
        }
    }

    // 搜索
    fun searchByKeyword(keyword: String) {
        _flowLst.value = colorList.filter {
            it.NAME.contains(keyword) || it.pinyin.contains(keyword)
        }
    }

    private fun Context.getLocaleStringResource(
        resourceId: Int,
        localeName: String = "zh-CN"
    ): String {
        val config = Configuration(resources.configuration)
        config.setLocale(Locale(localeName))
        return createConfigurationContext(config).getText(resourceId).toString()
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