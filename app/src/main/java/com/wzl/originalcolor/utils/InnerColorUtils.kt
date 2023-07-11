package com.wzl.originalcolor.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

/**
 * @Author lu
 * @Date 2023/5/9 09:06
 * @ClassName: ColorUtils
 * @Description:
 */
object InnerColorUtils {

    // 获取适应背景色的文本颜色
    @ColorInt
    fun getAdaptiveColor(@ColorInt color: Int): Int {
        val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, color)
        val blackContrast = ColorUtils.calculateContrast(Color.BLACK, color)
        return if (whiteContrast > blackContrast) color.setBrightness(0.3F) else color.setBrightness(-0.1F)
    }

    @ColorInt
    fun @receiver:ColorInt Int.setBrightness(brightness: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(this, hsv)
        hsv[2] = hsv[2] + brightness
        return Color.HSVToColor(hsv)
    }

    @ColorInt
    fun @receiver:ColorInt Int.setAlpha(alpha: Float): Int {
        return ColorUtils.setAlphaComponent(this, (alpha * 255).roundToInt())
    }
}