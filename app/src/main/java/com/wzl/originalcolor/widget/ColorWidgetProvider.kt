package com.wzl.originalcolor.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import com.wzl.originalcolor.Config
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.PxExtensions.dp
import com.wzl.originalcolor.utils.RemoteViewsUtil
import com.wzl.originalcolor.utils.SpUtil

class ColorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 获取当前小组件颜色
        val periodRefresh = SpUtil.getWidgetRefreshState(context)
        val originalColor: OriginalColor = if (periodRefresh) {
            ColorData.getWidgetColor(context)
        } else {
            ColorData.getThemeColor(context)
        }
        appWidgetIds.forEach { appWidgetId ->
            val remoteViews = RemoteViewsUtil.getWideWidgetView(context, originalColor)
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (context == null) return
        val minWidth: Int = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight: Int = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        val rows = getCellSize(minWidth)
        val columns = getCellSize(minHeight)
        val periodRefresh = SpUtil.getWidgetRefreshState(context)
        val originalColor: OriginalColor = if (periodRefresh) {
            ColorData.getWidgetColor(context)
        } else {
            ColorData.getThemeColor(context)
        }
        val remoteViews = if (rows <= 2 || columns == 1) {
            RemoteViewsUtil.getSmallWidgetView(context, originalColor)
        } else {
            RemoteViewsUtil.getWideWidgetView(context, originalColor)
        }
        appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
    }

    private fun getCellSize(size: Int): Int {
        var n = 2
        while (70 * n - 30 < size) {
            n++
        }
        return n - 1
    }
}