package com.linhnt.notifications.model

data class QueuedTransaction(
    var id: Long = 0L,
    var eventId: String = "",
    var notificationKey: String = "",
    var packageName: String = "",
    var deviceId: String = "",
    var app: String = "",
    var content: String = "",
    var source: String = "",
    var amount: String = "",
    var account: String = "",
    var time: String = "",
    var postTime: Long = 0L,
    var status: Boolean = false,
    var deliveryState: String = DeliveryState.PENDING,
    var attemptCount: Int = 0,
    var lastError: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
)
