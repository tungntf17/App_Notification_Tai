package com.linhnt.notifications.helper

import java.security.MessageDigest

object EventIdFactory {
    fun create(
        packageName: String,
        notificationKey: String,
        notificationId: Int,
        notificationTag: String,
        eventTime: Long
    ): String {
        val raw = listOf(
            packageName,
            notificationKey,
            notificationId.toString(),
            notificationTag,
            eventTime.toString()
        ).joinToString("|")
        return sha256(raw)
    }

    fun shortWorkId(raw: String): String = sha256(raw).take(32)

    private fun sha256(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
