package com.wzl.originalcolor.viewmodel

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wzl.originalcolor.COLORS
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Collections
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * @Author lu
 * @Date 2023/7/22 16:13
 * @ClassName: ColorViewModel
 * @Description:
 */
class ColorViewModel : ViewModel() {

    private var colorList: List<OriginalColor> = mutableListOf()

    fun getRandomColor(context: Context): OriginalColor? {
        checkColorList(context)
        return if (colorList.isEmpty()) {
            null
        } else {
            val max = colorList.size
            colorList[Random.nextInt(0, max - 1)]
        }
    }

    fun getColorList(context: Context): List<OriginalColor> {
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
        Collections.sort(tempColorList, object : Comparator<OriginalColor> {
            override fun compare(o1: OriginalColor?, o2: OriginalColor?): Int {
                if (o1 == null || o2 == null) {
                    return -1
                }
                return if (rgbToHsv(o1.getRGBColor())[0] == rgbToHsv(o2.getRGBColor())[0]) {
                    (rgbToHsv(o2.getRGBColor())[1] - rgbToHsv(o1.getRGBColor())[1]).roundToInt()
                } else {
                    (rgbToHsv(o2.getRGBColor())[0] - rgbToHsv(o1.getRGBColor())[0]).roundToInt()
                }
            }
        })
        colorList = tempColorList
        return colorList
    }

    // 其他：淡肉色 棕色 粉色 褐色 赭 醉瓜肉 淡咖啡 金驼
    fun getColorListByTag(context: Context, tag: String): List<OriginalColor> {
        checkColorList(context)
        if (tag == context.getString(R.string.chip_full)) {
            return colorList
        }
        return if (tag != context.getString(R.string.chip_other)) {
            colorList.filter { it.NAME.last().toString() == tag }
        } else {
            colorList.filter {
                !COLORS.contains(it.NAME.last().toString())
            }
        }
    }

    fun searchColorByKeyword(context: Context, keyword: String): List<OriginalColor> {
        checkColorList(context)
        return colorList.filter { it.NAME.contains(keyword) }
    }

    private fun checkColorList(context: Context) {
        colorList.ifEmpty {
            getColorList(context)
        }
    }

    private fun rgbToHsv(color: Int): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv
    }
}