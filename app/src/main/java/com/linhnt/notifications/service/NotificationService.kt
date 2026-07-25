package com.linhnt.notifications.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.eup.hanzii.databases.history_sqlite.HistorySQLiteDatabase
import com.google.gson.Gson
import com.linhnt.notifications.R
import com.linhnt.notifications.activity.MainActivity
import com.linhnt.notifications.helper.BCrypt
import com.linhnt.notifications.helper.PreferenceHelper
import com.linhnt.notifications.model.NotifyItem
import com.linhnt.notifications.model.PostData
import com.linhnt.notifications.model.ResultItem
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class NotificationService : NotificationListenerService() {
    var context: Context? = null
    val historySQLiteDatabase = HistorySQLiteDatabase(this, HistorySQLiteDatabase.DB_NAME)
    var lastPostData: PostData? = null
    val notificationId: Int = 7654

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        val foregroundNotification = getForegroundNotification()
        startForeground(notificationId, foregroundNotification)
    }

    private fun getForegroundNotification(): Notification {
        val channelId = "chat_head_chanel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // The user-visible name of the channel.
            val name: CharSequence = "Android-Notifications"
            // The user-visible description of the channel.
            val description = "Listen Android notifications event"
            val mChannel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
            // Configure the notification channel.
            mChannel.description = description
            mChannel.enableLights(true)
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.BLUE
            notificationManager.createNotificationChannel(mChannel)
        }

        val notifyIntent = Intent(applicationContext, MainActivity::class.java)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 1, notifyIntent, flags
        )

        return NotificationCompat.Builder(this, channelId).setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context!!.getString(R.string.list_empty_desc)).build()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val packageName = sbn.packageName
        val extras = sbn.notification.extras

        val titleData = extras.getString("android.title") ?: ""
        val messageData = (extras.getCharSequence("android.text") ?: "").toString()

        val postData = PostData()
        postData.content = "$titleData $messageData".replace("QR-", "")

        if (postData.content == lastPostData?.content) { // check trung hoan toan || truong hop chua luu vao db
            return
        }

        if (packageName.lowercase().contains("notifications1".lowercase())
            || packageName.lowercase().contains("VCB".lowercase())
            || packageName.lowercase().contains("momotransfer".lowercase())
        ) {
            var time: String = getCurrentTime()
            var status = false // gửi lên sv thành công

            val preferenceHelper = PreferenceHelper(context!!, "PREF_NOTIFY")
            postData.device_id = preferenceHelper.getDeviceId()
            postData.app = "Momo"

            val appNames = preferenceHelper.getAppNames()
            var pattern = Pattern.compile("([\\d.]{4,})[đd].+?\"(.+?) (.+?)\"")
            var matcher = pattern.matcher(postData.content.lowercase())

            if (matcher.find()) {
                postData.amount = matcher.group(1)!!.replace(".", "").trim()
                postData.source = matcher.group(2)!!.replace(" ", "").trim()
                postData.account = matcher.group(3)!!.replace(" ", "").trim()

            } else {
                pattern = Pattern.compile("([\\d.]{4,})[đd].+?($appNames) (\\w+)")
                matcher = pattern.matcher(postData.content.lowercase())

                if (matcher.find()) {
                    postData.amount = matcher.group(1)!!.replace(".", "").trim()
                    postData.source = matcher.group(2)!!.replace(" ", "").trim()
                    postData.account = matcher.group(3)!!.replace(" ", "").trim()

                } else {
                    postData.app = "VCB"
                    pattern = Pattern.compile("\\+([\\d,]{4,}) vnd.+?(\\d+-\\d+-\\d+ \\d+:\\d+:\\d+).+?($appNames) (\\w+)(-d{6,8}-d{2}:d{2}:d{2} d+)*")
                    matcher = pattern.matcher(postData.content.lowercase())
                    if (matcher.find()) {
                        postData.amount = matcher.group(1)!!.replace(",", "").trim()
                        time = matcher.group(2)!!
                        postData.source = matcher.group(3)!!.replace(" ", "").trim()
                        postData.account = matcher.group(4)!!.replace(" ", "").trim()
                    }
                }
            }

            //  check trùng lặp (duplicate) || chắc làm db luôn
            if (historySQLiteDatabase.dbHelper.checkDuplicate(postData.account, postData.amount, time)) {
                return
            }

            // account+device_id || bcrypt rounds: 8
            if (postData.account != "" && postData.amount != "") {
                val key = postData.account + postData.device_id
                val integrity = BCrypt.hashpw(key.trim(), BCrypt.gensalt(8))
                postData.integrity = integrity
            } else {
                postData.account = "lỗi"
                postData.app = packageName
            }

            try { // ok-http post to server
                val postServer = PostServer()
                val json = postServer.convertJson(postData)
                val response = postServer.post("http://103.139.202.23:3006/api/forwarder", json)

                if (!response.isNullOrEmpty()) {
                    val resultItem = Gson().fromJson(response, ResultItem::class.java)
                    if (resultItem != null && resultItem.success == true) {
                        status = true
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

//            val msgrcv = Intent("Msg")
//            msgrcv.putExtra("json", Gson().toJson(postData))
//            msgrcv.putExtra("time", time)
//            msgrcv.putExtra("status", status)
//            LocalBroadcastManager.getInstance(context!!).sendBroadcast(msgrcv)

            val item = NotifyItem()
            item.convertFromData(data = postData, time, status)
            historySQLiteDatabase.dbHelper.saveHistory(item)

            // luu lastPostData
            lastPostData = postData
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val sim = SimpleDateFormat("HH:mm dd-MM-yyyy")
        return sim.format(Date())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d("Msg", "Notification Removed")
    }
}