package com.wzl.originalcolor

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.wzl.originalcolor.model.OriginalColor
import com.wzl.originalcolor.utils.ColorData
import com.wzl.originalcolor.utils.RemoteViewsUtil

class ColorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val originalColor = ColorData.getThemeColor(context)
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
        val minWidth = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) ?: 0
        val minHeight = newOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) ?: 0
        if (context != null && minHeight >= 0) {
            val originalColor = ColorData.getThemeColor(context)
            val remoteViews = if (minHeight < 200) {
                RemoteViewsUtil.getSmallWidgetView(context,  originalColor)
            } else if (minWidth <= 350) {
                RemoteViewsUtil.getSmallWidgetView(context,  originalColor)
            } else {
                RemoteViewsUtil.getWideWidgetView(context, originalColor)
            }
            appWidgetManager?.updateAppWidget(appWidgetId, remoteViews)
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}