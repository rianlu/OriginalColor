package com.wzl.originalcolor.utils

import android.content.Context
import com.wzl.originalcolor.Config

object SpUtil {

    fun getLocalThemeColor(context: Context): String {
        val appColorSp = context.applicationContext.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        return appColorSp.getString(
            Config.SP_PARAM_THEME_COLOR, Config.DEFAULT_THEME_COLOR
        ) ?: Config.DEFAULT_THEME_COLOR
    }

    fun saveLocalThemeColor(context: Context, themeColor: String) {
        val appColorSp = context.applicationContext.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        appColorSp.edit().apply {
            putString(Config.SP_PARAM_THEME_COLOR, themeColor)
            apply()
        }
    }

    fun getWidgetColor(context: Context): String {
        val appColorSp = context.applicationContext.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        val themeColor = getLocalThemeColor(context)
        return appColorSp.getString(
            Config.SP_PARAM_WIDGET_COLOR, themeColor
        ) ?: themeColor
    }

    fun saveWidgetColor(context: Context, widgetColor: String) {
        val appColorSp = context.applicationContext.getSharedPreferences(
            Config.SP_APP_COLOR, Context.MODE_PRIVATE
        )
        appColorSp.edit().apply {
            putString(Config.SP_PARAM_WIDGET_COLOR, widgetColor)
            apply()
        }
    }

    // 震动相关
//    fun getVibrationState(context: Context): Boolean {
//        val settingsSp = context.applicationContext.getSharedPreferences(
//            Config.SP_SETTINGS, Context.MODE_PRIVATE
//        )
//        return settingsSp.getBoolean(
//            Config.SP_PARAM_VIBRATION, Config.DEFAULT_VIBRATION
//        )
//    }
//
//    fun saveVibrationState(context: Context, isChecked: Boolean) {
//        val settingsSp = context.applicationContext.getSharedPreferences(
//            Config.SP_SETTINGS, Context.MODE_PRIVATE
//        )
//        settingsSp.edit().apply {
//            putBoolean(Config.SP_PARAM_VIBRATION, isChecked)
//            apply()
//        }
//    }

    // 触觉反馈
    fun getHapticFeedbackState(context: Context): Boolean {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        return settingsSp.getBoolean(
            Config.SP_PARAM_HAPTIC_FEEDBACK, Config.DEFAULT_HAPTIC_FEEDBACK
        )
    }

    fun saveHapticFeedbackState(context: Context, isChecked: Boolean) {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        settingsSp.edit().apply {
            putBoolean(Config.SP_PARAM_HAPTIC_FEEDBACK, isChecked)
            apply()
        }
    }

    fun getWidgetRefreshState(context: Context): Boolean {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        return settingsSp.getBoolean(
            Config.SP_PARAM_PERIOD_REFRESH_WIDGET,
            Config.DEFAULT_PERIOD_REFRESH_WIDGET
        )
    }

    fun saveWidgetRefreshState(context: Context, isChecked: Boolean) {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        settingsSp.edit().apply {
            putBoolean(Config.SP_PARAM_PERIOD_REFRESH_WIDGET, isChecked)
            apply()
        }
    }

    fun getPrivacyPolicyState(context: Context): Boolean {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        return settingsSp.getBoolean(
            Config.SP_PARAM_PRIVACY_POLICY,
            Config.DEFAULT_PARAM_PRIVACY_POLICY
        )
    }

    fun savePrivacyPolicyState(context: Context, isAgreed: Boolean) {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        settingsSp.edit().apply {
            putBoolean(Config.SP_PARAM_PRIVACY_POLICY, isAgreed)
            apply()
        }
    }

    fun getMIUIShortcutState(context: Context): Boolean {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        return settingsSp.getBoolean(
            Config.SP_PARAM_MIUI_SHORTCUT,
            Config.DEFAULT_MIUI_SHORTCUT
        )
    }

    fun saveMIUIShortcutState(context: Context, isAgreed: Boolean) {
        val settingsSp = context.applicationContext.getSharedPreferences(
            Config.SP_SETTINGS, Context.MODE_PRIVATE
        )
        settingsSp.edit().apply {
            putBoolean(Config.SP_PARAM_MIUI_SHORTCUT, isAgreed)
            apply()
        }
    }
}