package com.wzl.originalcolor.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import kotlin.math.pow
import kotlin.math.sqrt

object ScreenUtil {

    fun isPad(context: Context): Boolean {
        val manager: DisplayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val outMetrics = DisplayMetrics()
        val defaultDisplay = manager.getDisplay(Display.DEFAULT_DISPLAY)
        defaultDisplay.getMetrics(outMetrics)
        val x = (outMetrics.widthPixels / outMetrics.xdpi).pow(2)
        val y = (outMetrics.heightPixels / outMetrics.ydpi).pow(2)
        val screenInch = sqrt(x + y)
        return screenInch >= 7
    }
}