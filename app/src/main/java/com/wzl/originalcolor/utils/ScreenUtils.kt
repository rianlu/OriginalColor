package com.wzl.originalcolor.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.pow
import kotlin.math.sqrt

object ScreenUtils {

    fun isPad(context: Context): Boolean {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        val x = (outMetrics.widthPixels / outMetrics.xdpi).pow(2)
        val y = (outMetrics.heightPixels / outMetrics.ydpi).pow(2)
        val screenInch = sqrt(x + y)
        return screenInch >= 7
    }
}