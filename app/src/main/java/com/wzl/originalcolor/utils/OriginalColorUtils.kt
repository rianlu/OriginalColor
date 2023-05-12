package com.wzl.originalcolor.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wzl.originalcolor.COLORS
import com.wzl.originalcolor.R
import com.wzl.originalcolor.model.OriginalColor
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.random.Random

/**
 * @Author lu
 * @Date 2023/4/29 01:18
 * @ClassName: ColorUtils
 * @Description:
 */
object OriginalColorUtils {

    private lateinit var colorList: List<OriginalColor>

    fun getRandomColor(context: Context): OriginalColor? {
        checkColorList(context)
        return if (colorList.isEmpty()) {
            null
        } else {
            val max = colorList.size
            colorList[Random.nextInt(0, max - 1)]
        }
    }

    fun getAllColors(context: Context): List<OriginalColor> {
        if (this::colorList.isInitialized) {
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
        colorList =
            Gson().fromJson(jsonString, object : TypeToken<ArrayList<OriginalColor>>() {}.type)
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
        if (this::colorList.isInitialized) {
            colorList
        } else {
            getAllColors(context)
        }
    }
}