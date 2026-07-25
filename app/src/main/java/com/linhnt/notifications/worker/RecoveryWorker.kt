package com.linhnt.notifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.linhnt.notifications.helper.HistorySQLiteDatabase

class RecoveryWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result {
        val ids = HistorySQLiteDatabase.getInstance(applicationContext)
            .dbHelper
            .getRetryableEventIds()
        ids.forEach { QueueScheduler.enqueueUpload(applicationContext, it) }
        return Result.success()
    }
}
