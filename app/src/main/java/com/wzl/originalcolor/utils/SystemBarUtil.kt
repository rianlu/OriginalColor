package com.wzl.originalcolor.utils

import android.app.Activity
import android.graphics.Color
import androidx.core.view.WindowCompat

object SystemBarUtil {

    fun setSystemBarAppearance(
        activity: Activity,
        statusBarColor: Int = Color.WHITE,
        navigationBarColor: Int = Color.WHITE,
        isAppearanceLightStatusBars: Boolean = true,
        isAppearanceLightNavigationBars: Boolean = true
    ) {
        activity.window.let { window ->
            window.statusBarColor = statusBarColor
            window.navigationBarColor = navigationBarColor
            val windowInsetController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetController.isAppearanceLightStatusBars = isAppearanceLightStatusBars
            windowInsetController.isAppearanceLightNavigationBars = isAppearanceLightNavigationBars
        }
    }

    fun setStatusBarColor(activity: Activity, statusBarColor: Int = Color.WHITE) {
        activity.window.statusBarColor = statusBarColor
    }

    fun setNavigationBarColor(activity: Activity, navigationBarColor: Int = Color.WHITE) {
        activity.window.navigationBarColor = navigationBarColor
    }
}