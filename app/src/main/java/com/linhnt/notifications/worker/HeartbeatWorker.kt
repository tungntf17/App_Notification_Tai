package com.linhnt.notifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Periodic recovery for queued requests. Listener heartbeat is written by NotificationService. */
class HeartbeatWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    override fun doWork(): Result {
        QueueScheduler.enqueueAllPending(applicationContext)
        return Result.success()
    }
}
