package com.linhnt.notifications.helper

import android.app.Notification
import android.os.Bundle

object NotificationContentExtractor {
    private const val MAX_WORK_DATA_BYTES = 6000

    fun extract(notification: Notification): String {
        val extras = notification.extras ?: Bundle.EMPTY
        val parts = LinkedHashSet<String>()

        add(parts, extras.getCharSequence(Notification.EXTRA_TITLE))
        add(parts, extras.getCharSequence(Notification.EXTRA_TITLE_BIG))
        add(parts, extras.getCharSequence(Notification.EXTRA_TEXT))
        add(parts, extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        add(parts, extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        add(parts, extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))

        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        lines?.forEach { add(parts, it) }
        add(parts, notification.tickerText)

        return limitUtf8(parts.joinToString("\n"), MAX_WORK_DATA_BYTES)
    }

    private fun add(parts: MutableSet<String>, value: CharSequence?) {
        val text = value?.toString()?.trim().orEmpty()
        if (text.isNotEmpty()) parts.add(text)
    }

    private fun limitUtf8(value: String, maxBytes: Int): String {
        if (value.toByteArray(Charsets.UTF_8).size <= maxBytes) return value
        val builder = StringBuilder()
        var used = 0
        for (char in value) {
            val bytes = char.toString().toByteArray(Charsets.UTF_8).size
            if (used + bytes > maxBytes) break
            builder.append(char)
            used += bytes
        }
        return builder.toString()
    }
}
