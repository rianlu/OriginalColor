package com.wzl.originalcolor.viewmodel

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.floor

/**
 * @Author lu
 * @Date 2023/7/22 16:13
 * @ClassName: ColorViewModel
 * @Description:
 */
class ColorViewModel : ViewModel() {

    private var colorList: List<OriginalColor> = mutableListOf()
    private var filterList: List<OriginalColor> = mutableListOf()

    private var _flowLst = MutableStateFlow<List<OriginalColor>>(emptyList())
    var flowList = _flowLst
        private set

    fun initData(context: Context): List<OriginalColor> {
        if (colorList.isNotEmpty()) {
            return colorList
        }
        val stringBuilder = StringBuilder()
        try {
            val isr = InputStreamReader(context.assets.open("colors.json"))
            val br = BufferedReader(isr)
            var line: String?
            while (br.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            br.close()
            isr.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonString = stringBuilder.toString()
        val tempColorList: List<OriginalColor> =
            Gson().fromJson(jsonString, object : TypeToken<ArrayList<OriginalColor>>() {}.type)
        colorList = tempColorList.sortedWith <OriginalColor> { o1, o2 ->
            if (o1.NAME == "栗紫" || o2.NAME == "栗紫") {
                print(rgbToHsv(o2.getRGBColor())[1] - rgbToHsv(o1.getRGBColor())[1])
            }
            if (rgbToHsv(o1.getRGBColor())[0] == rgbToHsv(o2.getRGBColor())[0]) {
                floor(rgbToHsv(o2.getRGBColor())[1] - rgbToHsv(o1.getRGBColor())[1]).toInt()
            } else {
                floor(rgbToHsv(o2.getRGBColor())[0] - rgbToHsv(o1.getRGBColor())[0]).toInt()
            }
        }
        filterList = colorList
        _flowLst.value = colorList
        return colorList
    }

    // 其他：淡肉色 棕色 粉色 褐色 赭 醉瓜肉 淡咖啡 金驼
    fun getColorListByTag(context: Context, tag: String): List<OriginalColor> {
        if (tag == context.getString(R.string.chip_full)) {
            filterList = colorList
            return filterList
        }
        filterList =  if (tag != context.getString(R.string.chip_other)) {
            colorList.filter { it.NAME.last().toString() == tag }
        } else {
            colorList.filter {
                !COLOR_TAGS.contains(it.NAME.last().toString())
            }
        }
        return  filterList
    }

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

    fun searchColorByKeyword(keyword: String): List<OriginalColor> {
        filterList =  colorList.filter { it.NAME.contains(keyword) }
        return filterList
    }

    private fun rgbToHsv(color: Int): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv
    }
}

val COLOR_TAGS = arrayOf("白", "灰", "红", "橙", "黄", "绿", "青", "蓝", "紫")