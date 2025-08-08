package com.wzl.originalcolor.utils

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wzl.originalcolor.model.OriginalColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.floor
import kotlin.random.Random

object ColorData {

    private var colorList: List<OriginalColor> = mutableListOf()

    fun initData(context: Context): List<OriginalColor> {
        val chineseColorList = loadJson(context, "colors.json")
        val cfsColorList = loadJson(context, "cfs-color.json")
        val tempColorList = chineseColorList + cfsColorList
        colorList = tempColorList.sortedWith<OriginalColor> { o1, o2 ->
            if (rgbToHsv(o1.getRGBColor())[0] == rgbToHsv(o2.getRGBColor())[0]) {
                floor(rgbToHsv(o2.getRGBColor())[1] - rgbToHsv(o1.getRGBColor())[1]).toInt()
            } else {
                floor(rgbToHsv(o2.getRGBColor())[0] - rgbToHsv(o1.getRGBColor())[0]).toInt()
            }
        }
        return colorList
    }

    private fun loadJson(context: Context, jsonName: String): List<OriginalColor> {
        val stringBuilder = StringBuilder()
        try {
            val isr = InputStreamReader(context.assets.open(jsonName))
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
        return Gson().fromJson(stringBuilder.toString(), object : TypeToken<ArrayList<OriginalColor>>() {}.type)
    }

    fun getRandomColor(context: Context): OriginalColor {
        if (colorList.isEmpty()) {
            initData(context)
        }
        val randomPosition = Random.nextInt(0, colorList.size)
        return colorList[randomPosition]
    }

    fun getThemeColor(context: Context): OriginalColor {
        val hex = SpUtil.getLocalThemeColor(context)
        return findColor(context, hex) ?: getRandomColor(context)
    }

    fun getWidgetColor(context: Context): OriginalColor? {
        val hex = SpUtil.getWidgetColor(context)
        if (hex.isEmpty()) return null
        return findColor(context, hex)
    }

    fun clearWidgetColor(context: Context) {
        SpUtil.saveWidgetColor(context, "")
    }

    private fun findColor(context: Context, hex: String): OriginalColor? {
        if (colorList.isEmpty()) {
            initData(context)
        }
        return colorList.find { it.HEX == hex }
    }

    private fun rgbToHsv(color: Int): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv
    }
}