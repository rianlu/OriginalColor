package com.wzl.originalcolor.utils

import android.content.Context
import com.wzl.originalcolor.Config

object Once {
    fun shouldShow(context: Context, key: String, default: Boolean = true): Boolean {
        val sp = context.applicationContext.getSharedPreferences(Config.SP_SETTINGS, Context.MODE_PRIVATE)
        return sp.getBoolean(key, default)
    }
    fun setShown(context: Context, key: String) {
        val sp = context.applicationContext.getSharedPreferences(Config.SP_SETTINGS, Context.MODE_PRIVATE)
        sp.edit().putBoolean(key, false).apply()
    }
}

