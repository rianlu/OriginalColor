package com.wzl.originalcolor.utils

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

/**
 * @Author lu
 * @Date 2023/5/6 08:47
 * @ClassName: UiModeUtils
 * @Description:
 */
enum class UiMode() {
    LIGHT_MODE(),
    DARK_MODE(),
    FOLLOW_SYSTEM()
}
object UiModeUtils {

    fun setUiMode(context: Context, uiMode: UiMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            uiModeManager.setApplicationNightMode(
                when (uiMode) {
                    UiMode.LIGHT_MODE -> UiModeManager.MODE_NIGHT_NO
                    UiMode.DARK_MODE -> UiModeManager.MODE_NIGHT_YES
                    UiMode.FOLLOW_SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
                }
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(
                when (uiMode) {
                    UiMode.LIGHT_MODE -> AppCompatDelegate.MODE_NIGHT_NO
                    UiMode.DARK_MODE -> AppCompatDelegate.MODE_NIGHT_YES
                    UiMode.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }
    }

    fun currentSystemMode(context: Context): UiMode {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            when (uiModeManager.currentModeType) {
                UiModeManager.MODE_NIGHT_NO -> UiMode.LIGHT_MODE
                UiModeManager.MODE_NIGHT_YES -> UiMode.DARK_MODE
                UiModeManager.MODE_NIGHT_AUTO -> UiMode.FOLLOW_SYSTEM
                else -> UiMode.FOLLOW_SYSTEM
            }
        } else {
            when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_NO -> UiMode.LIGHT_MODE
                AppCompatDelegate.MODE_NIGHT_YES -> UiMode.DARK_MODE
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> UiMode.FOLLOW_SYSTEM
                else -> UiMode.FOLLOW_SYSTEM
            }
        }

    }
}