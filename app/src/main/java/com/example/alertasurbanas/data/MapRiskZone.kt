package com.example.alertasurbanas.data

data class MapRiskZone(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val riskLevel: String,
    val reportCount: Int,
    val radiusMeters: Double = 420.0
)
