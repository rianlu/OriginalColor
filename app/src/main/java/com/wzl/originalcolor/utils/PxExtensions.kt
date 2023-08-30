package com.wzl.originalcolor.utils

import android.content.Context
import android.util.TypedValue

/**
 * @Author lu
 * @Date 2023/5/3 01:18
 * @ClassName: PxExtensions
 * @Description:
 */
object PxExtensions {

    fun Int.dp(context: Context): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    fun Int.dip(context: Context): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}