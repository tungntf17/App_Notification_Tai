package com.linhnt.notifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/* DEPRECATED: One-shot upload is now handled in CaptureNotificationWorker */
class HeartbeatWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result = Result.success()
}
