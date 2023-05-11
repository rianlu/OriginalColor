package com.wzl.originalcolor.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        val tempColorList = if (this::colorList.isInitialized) {
            colorList
        } else {
            getAllColors(context)
        }

        return if (tempColorList.isEmpty()) {
            null
        } else {
            val max = tempColorList.size
            tempColorList[Random.nextInt(0, max - 1)]
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

    fun getRandomIndex(context: Context): Int {
        if (!this::colorList.isInitialized) {
            getAllColors(context)
        }
        val total = colorList.size
        return Random.nextInt(0, total - 1)
    }
}