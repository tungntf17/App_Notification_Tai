package com.linhnt.notifications.model

data class ParsedTransaction(
    val app: String,
    val source: String,
    val account: String,
    val amount: String
)
