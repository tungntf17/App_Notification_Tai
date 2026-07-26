package com.linhnt.notifications

import android.app.Application
import com.linhnt.notifications.config.ServerConfig

class NotificationsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServerConfig.init(this)
    }
}
