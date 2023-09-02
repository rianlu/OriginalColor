package com.wzl.originalcolor.utils

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wzl.originalcolor.Config
import com.wzl.originalcolor.model.OriginalColor
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.floor
import kotlin.random.Random

object ColorData {

    private var colorList: List<OriginalColor> = mutableListOf()

    fun initData(context: Context): List<OriginalColor> {
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
            if (rgbToHsv(o1.getRGBColor())[0] == rgbToHsv(o2.getRGBColor())[0]) {
                floor(rgbToHsv(o2.getRGBColor())[1] - rgbToHsv(o1.getRGBColor())[1]).toInt()
            } else {
                floor(rgbToHsv(o2.getRGBColor())[0] - rgbToHsv(o1.getRGBColor())[0]).toInt()
            }
        }
        return colorList
    }

    fun getRandomColor(context: Context): OriginalColor {
        if (colorList.isNotEmpty()) {
            initData(context)
        }
        val randomPosition = Random.nextInt(0, colorList.size)
        return colorList[randomPosition]
    }

    fun getThemeColor(context: Context): OriginalColor {
        val themeSp = context.getSharedPreferences(
            Config.SP_GLOBAL_THEME_COLOR, Context.MODE_PRIVATE)
        val hex = themeSp.getString(Config.SP_PARAM_THEME_COLOR, null)
        var originalColor: OriginalColor
        if (hex != null) {
            originalColor = findColor(context, hex)
        } else {
            originalColor = getRandomColor(context)
            themeSp.edit().also {
                it.putString(Config.SP_PARAM_THEME_COLOR, originalColor.HEX)
                it.apply()
            }
        }
        return originalColor
    }

    private fun findColor(context: Context, hex: String): OriginalColor {
        if (colorList.isNotEmpty()) {
            initData(context)
        }
        return colorList.find { it.HEX.equals(hex) } ?: getRandomColor(context)
    }

    fun rgbToHsv(color: Int): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv
    }
}