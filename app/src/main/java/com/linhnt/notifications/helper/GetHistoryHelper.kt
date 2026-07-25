package com.linhnt.notifications.helper

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getStringOrNull
import com.linhnt.notifications.model.NotifyItem
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * Created by iDuyNM on 2/11/2019
 */
class GetHistoryHelper(
    private val sqliteOpenHelper: SQLiteOpenHelper,
) {
    val TABLE_HISTORY = "history_data"

    fun getTodayHistory(): ArrayList<NotifyItem> {
        val today = getToday()
        val query = "SELECT * FROM $TABLE_HISTORY Where time Like '%$today%' ORDER BY id desc"
        return getHistoryByQuery(query)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getToday(): String {
        val sim = SimpleDateFormat("dd-MM-yyyy")
        return sim.format(Date())
    }

    @SuppressLint("Range")
    private fun getHistoryByQuery(query: String): ArrayList<NotifyItem> {
        val result = ArrayList<NotifyItem>()
        try {
            val db = sqliteOpenHelper.readableDatabase
            try {
                val cursor = db.rawQuery(query, null)

                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val item = NotifyItem()
                    item.account = cursor.getStringOrNull(cursor.getColumnIndex("account")) ?: ""
                    item.app = cursor.getStringOrNull(cursor.getColumnIndex("app")) ?: ""
                    item.amount = cursor.getStringOrNull(cursor.getColumnIndex("amount")) ?: ""
                    item.time = cursor.getStringOrNull(cursor.getColumnIndex("time")) ?: ""
                    item.source = cursor.getStringOrNull(cursor.getColumnIndex("source")) ?: ""
                    item.status = cursor.getInt(cursor.getColumnIndex("status")) == 1

                    result.add(item)
                    cursor.moveToNext()
                }
                cursor.close()
            } catch (ex: SQLiteException) {
                ex.printStackTrace()
            } finally {
                db.close()
            }
        } catch (e: SQLiteDatabaseLockedException) {
        }

        return result
    }

    fun saveHistory(item: NotifyItem) {
        val db = sqliteOpenHelper.writableDatabase
        val newValue = ContentValues()

        newValue.put("account", item.account)
        newValue.put("app", item.app)
        newValue.put("amount", item.amount)
        newValue.put("time", item.time)
        newValue.put("source", item.source)
        newValue.put("status", item.status)

        db.insert(TABLE_HISTORY, null, newValue)
        db.close()
    }

    // check trung: cung account, cung tien, thoi gian khong qua 1 phut
    @SuppressLint("SimpleDateFormat")
    fun checkDuplicate(account: String, amount: String, time: String): Boolean {
        val today = getToday()
        val query = "SELECT * FROM $TABLE_HISTORY Where time Like '%$today%' and account = '$account' and amount = '$amount' and status = 1 ORDER BY id desc"
        val listHistory = getHistoryByQuery(query = query)

        // check date format pattern
        val patternMomo = Pattern.compile("\\d+:\\d+ \\d+-\\d+-\\d+")
        val matcherMomo= patternMomo.matcher(time)

        val defaultSimFormat = "HH:mm dd-MM-yyyy"
        val sim = SimpleDateFormat(defaultSimFormat)

        var newDate: Date? = null
        if (matcherMomo.find()) {
             newDate = sim.parse(time)
        }

        for (item in listHistory) {
            if (item.time == time) {
                return true
            }

            if (newDate != null) {
                val matcherItem = patternMomo.matcher(item.time)
                if (matcherItem.find()) {
                    val itemDate = sim.parse(item.time)
                    if (itemDate != null) {
                        if (abs(newDate.time - itemDate.time) < 1000) { // miliseconds
                            return  true
                        }
                    }
                }
            }
        }

        return false
    }

}