package com.example.alertasurbanas.model

data class UrbanReport(
    val id: String = "",
    val type: String = "",
    val description: String = "",
    val urgency: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String = "pending",
    val userId: String = "",
    val userName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String = ""
)
