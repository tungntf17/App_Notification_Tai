package com.linhnt.notifications.model

class NotifyItem {
    var eventId: String = ""
    var time: String = ""
    var app: String = ""
    var source: String = ""
    var amount: String = ""
    var account: String = ""
    var status: Boolean = false
    var deliveryState: String = DeliveryState.PENDING
    var lastError: String = ""
}
