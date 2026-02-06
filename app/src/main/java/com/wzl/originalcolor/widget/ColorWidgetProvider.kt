package com.wzl.originalcolor.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.RemoteViewsUtil
import com.wzl.originalcolor.utils.SpUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ColorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 是否开启定时刷新
                val periodRefresh = SpUtil.getWidgetRefreshState(context)
                val originalColor: OriginalColor = if (periodRefresh) {
                    ColorData.getWidgetColor(context) ?: ColorData.getThemeColor(context)
                } else {
                    ColorData.getThemeColor(context)
                }
                appWidgetIds.forEach { appWidgetId ->
                    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                    val rows = getCellSize(minWidth)
                    val columns = getCellSize(minHeight)
                    val remoteViews = if (rows == 1 || columns == 1) {
                        RemoteViewsUtil.getSmallWidgetView(context, originalColor)
                    } else {
                        RemoteViewsUtil.getWideWidgetView(context, originalColor)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (context == null || appWidgetManager == null) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val minWidth: Int =
                    newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
                val minHeight: Int =
                    newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
                val rows = getCellSize(minWidth)
                val columns = getCellSize(minHeight)
                val periodRefresh = SpUtil.getWidgetRefreshState(context)
                val originalColor: OriginalColor = if (periodRefresh) {
                    ColorData.getWidgetColor(context) ?: ColorData.getThemeColor(context)
                } else {
                    ColorData.getThemeColor(context)
                }
                val remoteViews = if (rows == 1 || columns == 1) {
                    RemoteViewsUtil.getSmallWidgetView(context, originalColor)
                } else {
                    RemoteViewsUtil.getWideWidgetView(context, originalColor)
                }
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun getCellSize(size: Int): Int {
        var n = 2
        while (70 * n - 30 < size) {
            n++
        }
        return n - 1
    }
}