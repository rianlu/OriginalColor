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
        val periodRefresh = SpUtil.getWidgetRefreshState(context)
        val widgetColor = SpUtil.getWidgetColor(context)
        val originalColor: OriginalColor
        // 定时刷新，生成新颜色
        if (periodRefresh &&
            widgetColor == Config.EMPTY_WIDGET_COLOR_BY_WORKER
        ) {
            originalColor = ColorData.getRandomColor(context)
            SpUtil.saveWidgetColor(context, originalColor.HEX)
        } else {
            originalColor = ColorData.getWidgetColor(context)
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
        val minWidth: Int = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight: Int = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0
        if (context != null) {
            val cellWidth = 57.dp(context)
            val cellHeight = 100
            val originalColor = ColorData.getWidgetColor(context)
            val remoteViews = if (minWidth <= cellWidth || minHeight <= cellHeight) {
                RemoteViewsUtil.getSmallWidgetView(context, originalColor)
            } else {
                RemoteViewsUtil.getWideWidgetView(context, originalColor)
            }
            appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}