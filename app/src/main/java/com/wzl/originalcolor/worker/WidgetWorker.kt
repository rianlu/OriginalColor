package com.wzl.originalcolor.worker

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wzl.originalcolor.Config
import com.wzl.originalcolor.utils.SpUtil
import com.wzl.originalcolor.widget.ColorWidgetProvider

class WidgetWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        // 清空保存的小组件颜色，通知重新生成颜色
        SpUtil.saveWidgetColor(applicationContext, Config.EMPTY_WIDGET_COLOR_BY_WORKER)
        applicationContext.sendBroadcast(Intent(applicationContext, ColorWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(applicationContext)
                .getAppWidgetIds(ComponentName(applicationContext, ColorWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        })
        return Result.success()
    }
}