package com.linhnt.notifications.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.linhnt.notifications.helper.EventIdFactory

object QueueScheduler {
    const val KEY_PACKAGE_NAME = "package_name"
    const val KEY_NOTIFICATION_KEY = "notification_key"
    const val KEY_NOTIFICATION_ID = "notification_id"
    const val KEY_NOTIFICATION_TAG = "notification_tag"
    const val KEY_POST_TIME = "post_time"
    const val KEY_EVENT_TIME = "event_time"
    const val KEY_CONTENT = "content"

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
}
