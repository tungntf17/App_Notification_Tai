package com.linhnt.notifications.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.MalformedJsonException
import java.text.SimpleDateFormat
import java.util.*

class PreferenceHelper(val context: Context, name: String) {

    private val shared: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private val KEY_DEVICE_ID = "KEY_DEVICE_ID"
    fun getDeviceId(): String {
        return shared.getString(KEY_DEVICE_ID, "")?.trim() ?: ""
    }

    fun setDeviceId(value: String) {
        shared.edit().putString(KEY_DEVICE_ID, value).apply()
    }

    private val APP_NAMES = "APP_NAMES"
    fun getAppNames(): String {
        val names = "accfifa|9dmanga|accffia|9dfgc|9dtt"
        return shared.getString(APP_NAMES, names)?.trim() ?: names
    }

    fun setAppNames(value: String) {
        shared.edit().putString(APP_NAMES, value).apply()
    }

}