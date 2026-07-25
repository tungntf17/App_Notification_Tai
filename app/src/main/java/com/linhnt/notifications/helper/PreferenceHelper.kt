package com.linhnt.notifications.helper

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class PreferenceHelper(context: Context, name: String = PREF_NAME) {
    private val shared: SharedPreferences = context.applicationContext
        .getSharedPreferences(name, Context.MODE_PRIVATE)

    fun getDeviceId(): String = shared.getString(KEY_DEVICE_ID, "")?.trim().orEmpty()

    fun setDeviceId(value: String) {
        shared.edit().putString(KEY_DEVICE_ID, value.trim()).apply()
    }

    fun getAppNames(): String {
        return shared.getString(KEY_APP_NAMES, DEFAULT_APP_NAMES)?.trim() ?: DEFAULT_APP_NAMES
    }

    fun getAppNameList(): List<String> {
        return getAppNames()
            .split('|', ',', ';', '\n')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinctBy { it.lowercase(Locale.ROOT) }
    }

    fun setAppNames(value: String) {
        shared.edit().putString(KEY_APP_NAMES, value.trim()).apply()
    }

    fun setListenerConnected(value: Boolean) {
        shared.edit()
            .putBoolean(KEY_LISTENER_CONNECTED, value)
            .putLong(KEY_LISTENER_STATE_AT, System.currentTimeMillis())
            .apply()
    }

    fun isListenerConnected(): Boolean = shared.getBoolean(KEY_LISTENER_CONNECTED, false)

    fun getListenerStateAt(): Long = shared.getLong(KEY_LISTENER_STATE_AT, 0L)

    fun setLastNotificationAt(value: Long = System.currentTimeMillis()) {
        shared.edit().putLong(KEY_LAST_NOTIFICATION_AT, value).apply()
    }

    fun getLastNotificationAt(): Long = shared.getLong(KEY_LAST_NOTIFICATION_AT, 0L)

    fun setLastHeartbeatAt(value: Long = System.currentTimeMillis()) {
        shared.edit().putLong(KEY_LAST_HEARTBEAT_AT, value).apply()
    }

    fun getLastHeartbeatAt(): Long = shared.getLong(KEY_LAST_HEARTBEAT_AT, 0L)

    companion object {
        const val PREF_NAME = "PREF_NOTIFY"
        private const val KEY_DEVICE_ID = "KEY_DEVICE_ID"
        private const val KEY_APP_NAMES = "APP_NAMES"
        private const val KEY_LISTENER_CONNECTED = "LISTENER_CONNECTED"
        private const val KEY_LISTENER_STATE_AT = "LISTENER_STATE_AT"
        private const val KEY_LAST_NOTIFICATION_AT = "LAST_NOTIFICATION_AT"
        private const val KEY_LAST_HEARTBEAT_AT = "LAST_HEARTBEAT_AT"
        private const val DEFAULT_APP_NAMES = "accfifa|9dmanga|accffia|9dfgc|9dtt"
    }
}
