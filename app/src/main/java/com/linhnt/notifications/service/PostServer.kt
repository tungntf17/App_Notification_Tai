package com.linhnt.notifications.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.linhnt.notifications.config.ServerConfig
import com.linhnt.notifications.model.PostData
import com.linhnt.notifications.model.ResultItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object PostServer {
    data class PostResult(
        val success: Boolean,
        val retryable: Boolean,
        val httpCode: Int? = null,
        val error: String = ""
    )

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(ServerConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ServerConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ServerConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(retryOnConnectionFailure = true)
        .build()

    fun post(data: PostData): PostResult {
        return try {
            val body = gson.toJson(data).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(ServerConfig.POST_URL)
                .header(ServerConfig.IDEMPOTENCY_HEADER, data.event_id)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                val resultItem = parseResult(responseBody)

                if (response.isSuccessful && (resultItem?.success == true)) {
                    return PostResult(success = true, retryable = false, httpCode = response.code)
                }

                val retryable = response.code == 408 || response.code == 429 || response.code >= 500
                val message = resultItem?.message
                    ?.takeIf { it.isNotBlank() }
                    ?: responseBody.take(1000).ifBlank { "HTTP ${response.code}" }

                PostResult(
                    success = false,
                    retryable = retryable,
                    httpCode = response.code,
                    error = message
                )
            }
        } catch (error: IOException) {
            PostResult(success = false, retryable = true, error = error.message ?: "Network error")
        } catch (error: IllegalArgumentException) {
            PostResult(success = false, retryable = false, error = error.message ?: "Invalid server URL")
        } catch (error: Exception) {
            PostResult(success = false, retryable = true, error = error.message ?: error.javaClass.simpleName)
        }
    }

    private fun parseResult(body: String): ResultItem? {
        if (body.isBlank()) return null
        return try {
            gson.fromJson(body, ResultItem::class.java)
        } catch (_: JsonSyntaxException) {
            null
        }
    }
}
