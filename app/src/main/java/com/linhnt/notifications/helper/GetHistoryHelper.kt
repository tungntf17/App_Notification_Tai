package com.linhnt.notifications.helper

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.linhnt.notifications.model.DeliveryState
import com.linhnt.notifications.model.NotifyItem
import com.linhnt.notifications.model.QueuedTransaction
import java.util.Calendar

class GetHistoryHelper(
    private val sqliteOpenHelper: SQLiteOpenHelper
) {
    private val table = HistorySQLiteDatabase.TABLE_HISTORY

    fun insert(item: QueuedTransaction): Boolean {
        val values = ContentValues().apply {
            put("event_id", item.eventId)
            put("notification_key", item.notificationKey)
            put("package_name", item.packageName)
            put("device_id", item.deviceId)
            put("app", item.app)
            put("source", item.source)
            put("amount", item.amount)
            put("account", item.account)
            put("time", item.time)
            put("post_time", item.postTime)
            put("content", item.content)
            put("status", if (item.status) 1 else 0)
            put("delivery_state", item.deliveryState)
            put("attempt_count", item.attemptCount)
            put("last_error", item.lastError)
            put("created_at", item.createdAt)
            put("updated_at", item.updatedAt)
        }
        val rowId = sqliteOpenHelper.writableDatabase.insertWithOnConflict(
            table,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        return rowId != -1L
    }

    fun insertPending(item: QueuedTransaction): Boolean {
        val values = ContentValues().apply {
            put("event_id", item.eventId)
            put("notification_key", item.notificationKey)
            put("package_name", item.packageName)
            put("device_id", item.deviceId)
            put("app", item.app)
            put("source", item.source)
            put("amount", item.amount)
            put("account", item.account)
            put("time", item.time)
            put("post_time", item.postTime)
            put("content", item.content)
            put("status", 0)
            put("delivery_state", DeliveryState.PENDING)
            put("attempt_count", 0)
            put("last_error", "")
            put("created_at", item.createdAt)
            put("updated_at", item.updatedAt)
        }
        val rowId = sqliteOpenHelper.writableDatabase.insertWithOnConflict(
            table,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
        )
        return rowId != -1L
    }

    fun getByEventId(eventId: String): QueuedTransaction? {
        val cursor = sqliteOpenHelper.readableDatabase.query(
            table,
            null,
            "event_id = ?",
            arrayOf(eventId),
            null,
            null,
            null,
            "1"
        )
        cursor.use {
            return if (it.moveToFirst()) readQueuedTransaction(it) else null
        }
    }

    fun markSending(eventId: String) {
        sqliteOpenHelper.writableDatabase.execSQL(
            """
            UPDATE $table
            SET delivery_state = ?, attempt_count = attempt_count + 1,
                last_error = '', updated_at = ?
            WHERE event_id = ? AND delivery_state != ?
            """.trimIndent(),
            arrayOf(DeliveryState.SENDING, System.currentTimeMillis(), eventId, DeliveryState.SENT)
        )
    }

    fun markRetry(eventId: String, error: String) {
        updateDelivery(eventId, DeliveryState.RETRY, false, error)
    }

    fun markSent(eventId: String) {
        updateDelivery(eventId, DeliveryState.SENT, true, "")
    }

    fun markFailed(eventId: String, error: String) {
        updateDelivery(eventId, DeliveryState.FAILED, false, error)
    }

    private fun updateDelivery(eventId: String, state: String, status: Boolean, error: String) {
        val values = ContentValues().apply {
            put("delivery_state", state)
            put("status", if (status) 1 else 0)
            put("last_error", error.take(1000))
            put("updated_at", System.currentTimeMillis())
        }
        sqliteOpenHelper.writableDatabase.update(
            table,
            values,
            "event_id = ?",
            arrayOf(eventId)
        )
    }

    fun getRetryableEventIds(limit: Int = 100): List<String> {
        val states = arrayOf(DeliveryState.PENDING, DeliveryState.RETRY, DeliveryState.SENDING)
        val cursor = sqliteOpenHelper.readableDatabase.query(
            table,
            arrayOf("event_id"),
            "delivery_state IN (?, ?, ?)",
            states,
            null,
            null,
            "created_at ASC",
            limit.coerceIn(1, 500).toString()
        )
        val result = ArrayList<String>()
        cursor.use {
            while (it.moveToNext()) {
                result.add(it.getString(it.getColumnIndexOrThrow("event_id")))
            }
        }
        return result
    }

    fun getPendingCount(): Int {
        val cursor = sqliteOpenHelper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $table WHERE delivery_state IN (?, ?, ?)",
            arrayOf(DeliveryState.PENDING, DeliveryState.RETRY, DeliveryState.SENDING)
        )
        cursor.use { return if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun getTodayHistory(): ArrayList<NotifyItem> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis
        val legacyDate = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(start))

        val cursor = sqliteOpenHelper.readableDatabase.query(
            table,
            null,
            "(post_time >= ? AND post_time < ?) OR (post_time = 0 AND time LIKE ?)",
            arrayOf(start.toString(), end.toString(), "%$legacyDate%"),
            null,
            null,
            "id DESC"
        )
        val result = ArrayList<NotifyItem>()
        cursor.use {
            while (it.moveToNext()) {
                result.add(
                    NotifyItem().apply {
                        eventId = it.string("event_id")
                        account = it.string("account")
                        app = it.string("app")
                        amount = it.string("amount")
                        time = it.string("time")
                        source = it.string("source")
                        status = it.int("status") == 1
                        deliveryState = it.string("delivery_state").ifBlank {
                            if (status) DeliveryState.SENT else DeliveryState.FAILED
                        }
                        lastError = it.string("last_error")
                    }
                )
            }
        }
        return result
    }

    private fun readQueuedTransaction(cursor: Cursor): QueuedTransaction {
        return QueuedTransaction(
            id = cursor.long("id"),
            eventId = cursor.string("event_id"),
            notificationKey = cursor.string("notification_key"),
            packageName = cursor.string("package_name"),
            deviceId = cursor.string("device_id"),
            app = cursor.string("app"),
            content = cursor.string("content"),
            source = cursor.string("source"),
            amount = cursor.string("amount"),
            account = cursor.string("account"),
            time = cursor.string("time"),
            postTime = cursor.long("post_time"),
            status = cursor.int("status") == 1,
            deliveryState = cursor.string("delivery_state"),
            attemptCount = cursor.int("attempt_count"),
            lastError = cursor.string("last_error"),
            createdAt = cursor.long("created_at"),
            updatedAt = cursor.long("updated_at")
        )
    }

    private fun Cursor.indexOrMinusOne(name: String): Int = getColumnIndex(name)
    private fun Cursor.string(name: String): String {
        val index = indexOrMinusOne(name)
        return if (index < 0 || isNull(index)) "" else getString(index)
    }
    private fun Cursor.int(name: String): Int {
        val index = indexOrMinusOne(name)
        return if (index < 0 || isNull(index)) 0 else getInt(index)
    }
    private fun Cursor.long(name: String): Long {
        val index = indexOrMinusOne(name)
        return if (index < 0 || isNull(index)) 0L else getLong(index)
    }

    companion object {
        private const val TAG = "HistoryDatabase"
    }
}
