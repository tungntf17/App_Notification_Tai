package com.linhnt.notifications

import android.app.Application
import com.linhnt.notifications.config.ServerConfig
import com.linhnt.notifications.worker.QueueScheduler

class NotificationsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServerConfig.init(this)
        QueueScheduler.ensureHeartbeat(this)
        QueueScheduler.enqueueAllPending(this)
    }
}
