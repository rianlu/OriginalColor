package com.wzl.originalcolor.utils

import android.content.Context
import android.util.TypedValue

/**
 * @Author lu
 * @Date 2023/5/3 01:18
 * @ClassName: PxUtils
 * @Description:
 */
object PxUtils {

    fun dp2px(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.getResources().getDisplayMetrics()
        ).toInt()
    }

    fun sp2px(context: Context, sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp.toFloat(),
            context.getResources().getDisplayMetrics()
        ).toInt()
    }
}