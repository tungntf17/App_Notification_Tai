package com.linhnt.notifications.config

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Chỉ cần sửa file này khi đổi API server.
 */
object ServerConfig {
    var POST_URL = "http://103.139.202.23:3006/api/forwarder"
        private set

    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    const val IDEMPOTENCY_HEADER = "Idempotency-Key"

    private const val CONFIG_FOLDER_NAME = "NotificationsConfig"
    private const val CONFIG_FILE_NAME = "server_url.txt"

    fun init(context: Context) {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val configFolder = File(downloadDir, CONFIG_FOLDER_NAME)
        
        if (!configFolder.exists()) {
            configFolder.mkdirs()
        }

        val configFile = File(configFolder, CONFIG_FILE_NAME)
        if (!configFile.exists()) {
            try {
                configFile.writeText(POST_URL)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val savedUrl = configFile.readText().trim()
                if (savedUrl.isNotBlank()) {
                    POST_URL = savedUrl
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
