package com.wzl.originalcolor.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

/**
 * @Author lu
 * @Date 2023/5/9 09:06
 * @ClassName: ColorUtils
 * @Description:
 */
object InnerColorUtils {

    // 获取适应背景色的文本颜色
    @ColorInt
    fun getContrastColor(@ColorInt color: Int): Int {
        val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, color)
        val blackContrast = ColorUtils.calculateContrast(Color.BLACK, color)
        return if (whiteContrast > blackContrast) getBrighterColor(color) else getDarkerColor(color)
    }

    // 获取更深颜色
    @ColorInt
    fun getDarkerColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv) // convert to hsv
        // make darker
        hsv[1] = hsv[1] + 0.1f // 饱和度更高
        hsv[2] = hsv[2] - 0.1f // 明度降低
        return Color.HSVToColor(hsv)
    }

    // 获取更浅的颜色
    @ColorInt
    fun getBrighterColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv) // convert to hsv

        hsv[1] = hsv[1] - 0.3f // 降低饱和度
        hsv[2] = hsv[2] + 0.3f // 提升亮度
        return Color.HSVToColor(hsv)
    }
}