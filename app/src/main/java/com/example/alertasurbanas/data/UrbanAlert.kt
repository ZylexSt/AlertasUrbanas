package com.example.alertasurbanas.data

data class UrbanAlert(
    val id: String,
    val title: String,
    val category: String,
    val address: String,
    val description: String,
    val urgency: String,
    val distanceText: String,
    val timeText: String,
    val latitude: Double,
    val longitude: Double
)
