package com.wzl.originalcolor.utils

import android.content.Context
import com.wzl.originalcolor.Config

object SpUtil {

    fun getLocalThemeColor(context: Context): String {
        val appColorSp = context.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        return appColorSp.getString(
            Config.SP_PARAM_THEME_COLOR, Config.DEFAULT_THEME_COLOR
        ) ?: Config.DEFAULT_THEME_COLOR
    }

    fun saveLocalThemeColor(context: Context, themeColor: String) {
        val appColorSp = context.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        appColorSp.edit().apply {
            putString(Config.SP_PARAM_THEME_COLOR, themeColor)
            apply()
        }
    }

    fun getWidgetColor(context: Context): String {
        val appColorSp = context.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        val themeColor = getLocalThemeColor(context)
        return appColorSp.getString(
            Config.SP_PARAM_WIDGET_COLOR, themeColor
        ) ?: themeColor
    }

    fun saveWidgetColor(context: Context, widgetColor: String) {
        val appColorSp = context.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        appColorSp.edit().apply {
            putString(Config.SP_PARAM_WIDGET_COLOR, widgetColor)
            apply()
        }
    }

    fun getVibrationState(context: Context): Boolean {
        val settingsSp = context.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        return settingsSp.getBoolean(
            Config.SP_PARAM_VIBRATION, Config.DEFAULT_VIBRATION
        )
    }

    fun saveVibrationState(context: Context, isChecked: Boolean) {
        val settingsSp = context.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        settingsSp.edit().apply {
            putBoolean(Config.SP_PARAM_VIBRATION, isChecked)
            apply()
        }
    }

    fun getWidgetRefreshState(context: Context): Boolean {
        val settingsSp = context.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        return settingsSp.getBoolean(
            Config.SP_PARAM_PERIOD_REFRESH_WIDGET,
            Config.DEFAULT_PERIOD_REFRESH_WIDGET
        )
    }

    fun saveWidgetRefreshState(context: Context, isChecked: Boolean) {
        val settingsSp = context.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        settingsSp.edit().apply {
            putBoolean(Config.SP_PARAM_PERIOD_REFRESH_WIDGET, isChecked)
            apply()
        }
    }
}