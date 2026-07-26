package com.linhnt.notifications.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.linhnt.notifications.config.SupportedBankApps
import com.linhnt.notifications.helper.BCrypt
import com.linhnt.notifications.helper.BankNotificationParser
import com.linhnt.notifications.helper.EventIdFactory
import com.linhnt.notifications.helper.HistorySQLiteDatabase
import com.linhnt.notifications.helper.PreferenceHelper
import com.linhnt.notifications.model.DeliveryState
import com.linhnt.notifications.model.PostData
import com.linhnt.notifications.model.QueuedTransaction
import com.linhnt.notifications.service.PostServer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaptureNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val packageName = inputData.getString(QueueScheduler.KEY_PACKAGE_NAME).orEmpty()
        if (!SupportedBankApps.isSupported(packageName)) return Result.success()

        val notificationKey = inputData.getString(QueueScheduler.KEY_NOTIFICATION_KEY).orEmpty()
        val notificationId = inputData.getInt(QueueScheduler.KEY_NOTIFICATION_ID, 0)
        val notificationTag = inputData.getString(QueueScheduler.KEY_NOTIFICATION_TAG).orEmpty()
        val postTime = inputData.getLong(QueueScheduler.KEY_POST_TIME, System.currentTimeMillis())
        val eventTime = inputData.getLong(QueueScheduler.KEY_EVENT_TIME, postTime).takeIf { it > 0L } ?: postTime
        val content = inputData.getString(QueueScheduler.KEY_CONTENT).orEmpty()
        if (content.isBlank()) return Result.success()

        val preferences = PreferenceHelper(applicationContext)
        val parsed = BankNotificationParser.parse(
            packageName = packageName,
            rawContent = content,
            sourceNames = preferences.getAppNameList()
        )
        if (parsed == null) {
            Log.w(TAG, "Ignored unparseable notification from $packageName")
            return Result.success()
        }

        val eventId = EventIdFactory.create(
            packageName = packageName,
            notificationKey = notificationKey,
            notificationId = notificationId,
            notificationTag = notificationTag,
            eventTime = eventTime
        )

        // Thực hiện gửi POST ngay lập tức
        val deviceId = preferences.getDeviceId()
        val integrityKey = (parsed.account + deviceId).trim()
        val postData = PostData(
            integrity = BCrypt.hashpw(integrityKey, BCrypt.gensalt(8)),
            event_id = eventId,
            device_id = deviceId,
            app = parsed.app,
            content = content,
            source = parsed.source,
            amount = parsed.amount,
            account = parsed.account
        )
        val response = PostServer.post(postData)

        val now = System.currentTimeMillis()
        val isSuccess = response.success || response.isDuplicate
        val item = QueuedTransaction(
            eventId = eventId,
            notificationKey = notificationKey,
            packageName = packageName,
            deviceId = deviceId,
            app = parsed.app,
            content = content,
            source = parsed.source,
            amount = parsed.amount,
            account = parsed.account,
            time = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
                .format(Date(eventTime)),
            postTime = eventTime,
            status = isSuccess,
            deliveryState = if (isSuccess) DeliveryState.SENT else DeliveryState.FAILED,
            attemptCount = 1,
            lastError = response.error,
            createdAt = now,
            updatedAt = now
        )

        val helper = HistorySQLiteDatabase.getInstance(applicationContext).dbHelper
        helper.insert(item)

        return Result.success()
    }

    companion object {
        private const val TAG = "CaptureNotification"
    }
}
