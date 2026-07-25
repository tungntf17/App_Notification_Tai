package com.linhnt.notifications.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.linhnt.notifications.helper.EventIdFactory
import java.util.concurrent.TimeUnit

object QueueScheduler {
    private const val HEARTBEAT_WORK = "notification-listener-heartbeat"
    private const val RECOVERY_WORK = "notification-upload-recovery"

    const val KEY_PACKAGE_NAME = "package_name"
    const val KEY_NOTIFICATION_KEY = "notification_key"
    const val KEY_NOTIFICATION_ID = "notification_id"
    const val KEY_NOTIFICATION_TAG = "notification_tag"
    const val KEY_POST_TIME = "post_time"
    const val KEY_EVENT_TIME = "event_time"
    const val KEY_CONTENT = "content"
    const val KEY_EVENT_ID = "event_id"

    fun enqueueCapture(
        context: Context,
        packageName: String,
        notificationKey: String,
        notificationId: Int,
        notificationTag: String,
        postTime: Long,
        eventTime: Long,
        content: String
    ) {
        val workKey = listOf(packageName, notificationKey, notificationId, notificationTag, eventTime)
            .joinToString("|")
        val data = Data.Builder()
            .putString(KEY_PACKAGE_NAME, packageName)
            .putString(KEY_NOTIFICATION_KEY, notificationKey)
            .putInt(KEY_NOTIFICATION_ID, notificationId)
            .putString(KEY_NOTIFICATION_TAG, notificationTag)
            .putLong(KEY_POST_TIME, postTime)
            .putLong(KEY_EVENT_TIME, eventTime)
            .putString(KEY_CONTENT, content)
            .build()
        val request = OneTimeWorkRequestBuilder<CaptureNotificationWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "capture-${EventIdFactory.shortWorkId(workKey)}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun enqueueUpload(context: Context, eventId: String) {
        if (eventId.isBlank()) return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(Data.Builder().putString(KEY_EVENT_ID, eventId).build())
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "upload-$eventId",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun enqueueAllPending(context: Context) {
        val request = OneTimeWorkRequestBuilder<RecoveryWorker>().build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            RECOVERY_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun ensureHeartbeat(context: Context) {
        val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            HEARTBEAT_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
