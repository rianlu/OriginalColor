package com.wzl.originalcolor.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

/**
 * @Author lu
 * @Date 2023/5/9 09:06
 * @ClassName: ColorExtensions
 * @Description:
 */
object ColorExtensions {

    fun @receiver:ColorInt Int.isLight(): Boolean {
        val red = Color.valueOf(this).red()
        val green = Color.valueOf(this).green()
        val blue = Color.valueOf(this).blue()
        val brightness = (red * 299 + green * 587 + blue * 114) / 1000
        return brightness > 0.5
    }

    @ColorInt
    fun @receiver:ColorInt Int.setAlpha(alpha: Float): Int {
        return ColorUtils.setAlphaComponent(this, (alpha * 255).roundToInt())
    }

    @ColorInt
    fun @receiver:ColorInt Int.brightness(changeBrightness: Float): Int {
        val outHsl = FloatArray(3)
        ColorUtils.colorToHSL(this, outHsl)
        if (changeBrightness <= 0) {
            outHsl[2] = outHsl[2] * (1 + changeBrightness)
        } else {
            outHsl[2] = outHsl[2] + (1 - outHsl[2]) / 10 * changeBrightness * 10
        }
//        outHsl[2] = (outHsl[2] * (1 + changeBrightness))
        return ColorUtils.HSLToColor(outHsl)
    }
}