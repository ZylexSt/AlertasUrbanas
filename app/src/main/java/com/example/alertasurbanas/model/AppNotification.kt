package com.example.alertasurbanas.model

data class AppNotification(
    val id: String = "",
    val recipientId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info",
    val reportId: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)