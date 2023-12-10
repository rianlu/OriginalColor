package com.wzl.originalcolor.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wzl.originalcolor.Config
import com.wzl.originalcolor.worker.WidgetWorker
import java.util.concurrent.TimeUnit

object WorkManagerUtil {

    fun startWork(context: Context) {
        // 每小时刷新一次
        val widgetWorkRequest = PeriodicWorkRequestBuilder<WidgetWorker>(
            1L, TimeUnit.HOURS
        ).build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                Config.WIDGET_WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                widgetWorkRequest
            )
    }

    fun cancelWork(context: Context) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(Config.WIDGET_WORKER_TAG)
    }
}