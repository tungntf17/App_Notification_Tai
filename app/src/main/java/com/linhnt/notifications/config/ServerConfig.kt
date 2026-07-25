package com.linhnt.notifications.config

/**
 * Chỉ cần sửa file này khi đổi API server.
 */
object ServerConfig {
    const val POST_URL = "http://103.139.202.23:3006/api/forwarder"

    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    const val IDEMPOTENCY_HEADER = "Idempotency-Key"
}
