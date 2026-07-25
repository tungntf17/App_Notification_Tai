package com.linhnt.notifications

import android.app.Application
import com.linhnt.notifications.worker.QueueScheduler

class NotificationsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        QueueScheduler.ensureHeartbeat(this)
        QueueScheduler.enqueueAllPending(this)
    }
}
