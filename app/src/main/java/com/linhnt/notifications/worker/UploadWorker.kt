package com.linhnt.notifications.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.linhnt.notifications.helper.BCrypt
import com.linhnt.notifications.helper.HistorySQLiteDatabase
import com.linhnt.notifications.helper.PreferenceHelper
import com.linhnt.notifications.model.DeliveryState
import com.linhnt.notifications.model.PostData
import com.linhnt.notifications.service.PostServer

class UploadWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val eventId = inputData.getString(QueueScheduler.KEY_EVENT_ID).orEmpty()
        if (eventId.isBlank()) return Result.failure()

        val helper = HistorySQLiteDatabase.getInstance(applicationContext).dbHelper
        val item = helper.getByEventId(eventId) ?: return Result.success()
        if (item.deliveryState == DeliveryState.SENT || item.deliveryState == DeliveryState.FAILED) {
            return Result.success()
        }

        val currentDeviceId = item.deviceId.ifBlank {
            PreferenceHelper(applicationContext).getDeviceId()
        }
        if (currentDeviceId.isBlank()) {
            helper.markRetry(eventId, "device_id is empty")
            return Result.retry()
        }

        helper.markSending(eventId)
        val integrityKey = (item.account + currentDeviceId).trim()
        val postData = PostData(
            integrity = BCrypt.hashpw(integrityKey, BCrypt.gensalt(8)),
            event_id = eventId,
            device_id = currentDeviceId,
            app = item.app,
            content = item.content,
            source = item.source,
            amount = item.amount,
            account = item.account
        )
        val response = PostServer.post(postData)

        return when {
            response.success || response.isDuplicate -> {
                helper.markSent(eventId)
                Result.success()
            }
            response.retryable -> {
                helper.markRetry(eventId, response.error)
                Result.retry()
            }
            else -> {
                helper.markFailed(eventId, response.error)
                Result.success()
            }
        }
    }
}
