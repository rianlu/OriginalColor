package com.wzl.originalcolor.utils

import android.content.Context
import android.content.res.Configuration

/**
 * @Author lu
 * @Date 2023/5/6 08:47
 * @ClassName: UiModeUtil
 * @Description:
 */
enum class UiMode() {
    LIGHT_MODE(),
    DARK_MODE(),
    FOLLOW_SYSTEM()
}

object UiModeUtil {

    fun isDarkMode(context: Context): Boolean {
        val flag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return flag == Configuration.UI_MODE_NIGHT_YES
    }

    fun isLightMode(context: Context): Boolean {
        val flag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return flag == Configuration.UI_MODE_NIGHT_NO
    }
}