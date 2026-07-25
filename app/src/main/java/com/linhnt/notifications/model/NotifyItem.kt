package com.linhnt.notifications.model

class NotifyItem {
    fun convertFromData(data: PostData, time: String, status: Boolean) {
        this.app = data.app
        this.source = data.source
        this.amount = data.amount
        this.account = data.account
        this.time = time
        this.status = status
    }

    var time: String = ""
    var app: String = ""
    var source: String = ""
    var amount: String = ""
    var account: String = ""
    var status: Boolean = false
}