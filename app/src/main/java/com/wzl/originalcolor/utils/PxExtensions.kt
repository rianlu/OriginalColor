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
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    fun Int.sp(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}