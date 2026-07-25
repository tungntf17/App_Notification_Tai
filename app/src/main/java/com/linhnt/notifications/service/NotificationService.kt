package com.linhnt.notifications.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.linhnt.notifications.R
import com.linhnt.notifications.activity.MainActivity
import com.linhnt.notifications.config.SupportedBankApps
import com.linhnt.notifications.helper.NotificationContentExtractor
import com.linhnt.notifications.helper.PreferenceHelper
import com.linhnt.notifications.worker.QueueScheduler

class NotificationService : NotificationListenerService() {
    private val heartbeatHandler = Handler(Looper.getMainLooper())
    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            PreferenceHelper(this@NotificationService).setLastHeartbeatAt()
            heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        QueueScheduler.ensureHeartbeat(this)
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        PreferenceHelper(this).setListenerConnected(true)
        startServiceHeartbeat()
        QueueScheduler.enqueueAllPending(this)
        Log.i(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        PreferenceHelper(this).setListenerConnected(false)
        stopServiceHeartbeat()
        Log.w(TAG, "Notification listener disconnected")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NotificationService::class.java))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || !SupportedBankApps.isSupported(sbn.packageName)) return

        val content = NotificationContentExtractor.extract(sbn.notification)
        if (content.isBlank()) return

        PreferenceHelper(this).setLastNotificationAt()
        val eventTime = sbn.notification.`when`.takeIf { it > 0L } ?: sbn.postTime
        QueueScheduler.enqueueCapture(
            context = this,
            packageName = sbn.packageName,
            notificationKey = sbn.key.orEmpty(),
            notificationId = sbn.id,
            notificationTag = sbn.tag.orEmpty(),
            postTime = sbn.postTime,
            eventTime = eventTime,
            content = content
        )
    }

    override fun onDestroy() {
        PreferenceHelper(this).setListenerConnected(false)
        stopServiceHeartbeat()
        super.onDestroy()
    }

    private fun startServiceHeartbeat() {
        heartbeatHandler.removeCallbacks(heartbeatRunnable)
        heartbeatHandler.post(heartbeatRunnable)
    }

    private fun stopServiceHeartbeat() {
        heartbeatHandler.removeCallbacks(heartbeatRunnable)
    }

    private fun createForegroundNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Android Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Theo dõi thông báo giao dịch ngân hàng"
                enableLights(true)
                lightColor = Color.BLUE
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, pendingFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Đang theo dõi thông báo ngân hàng")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "NotificationService"
        private const val CHANNEL_ID = "notification_listener_status"
        private const val NOTIFICATION_ID = 7654
        private const val HEARTBEAT_INTERVAL_MS = 60_000L
    }
}
