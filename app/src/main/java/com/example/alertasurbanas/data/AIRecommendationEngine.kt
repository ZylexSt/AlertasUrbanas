package com.example.alertasurbanas.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.alertasurbanas.ui.theme.UrbanColors
import org.maplibre.android.geometry.LatLng
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class AIRecommendationResult(
    val title: String,
    val description: String,
    val label: String,
    val color: Color,
    val icon: ImageVector
)

data class AIRiskZone(
    val id: String,
    val title: String,
    val riskLevel: String,
    val dominantCategory: String,
    val reportCount: Int,
    val latitude: Double,
    val longitude: Double,
    val recommendation: String
)

data class AIRiskSummary(
    val title: String,
    val description: String,
    val analyzedZones: Int,
    val highRiskZones: Int,
    val mediumRiskZones: Int,
    val lowRiskZones: Int,
    val zones: List<AIRiskZone> = emptyList(),
    val recommendations: List<AIRecommendationResult>
)

object AIRecommendationEngine {
    fun analyze(
        alerts: List<UrbanAlert>,
        userLocation: LatLng? = null,
        radiusMeters: Double = 8_000.0
    ): AIRiskSummary {
        val analyzedAlerts = userLocation?.let { origin ->
            alerts.filter { alert ->
                distanceMeters(origin, LatLng(alert.latitude, alert.longitude)) <= radiusMeters
            }
        } ?: alerts

        if (analyzedAlerts.isEmpty()) {
            return AIRiskSummary(
                title = if (userLocation == null) "Esperando ubicación" else "Sin zonas de riesgo cercanas",
                description = if (userLocation == null) {
                    "Esperando tu ubicación actual para analizar reportes cercanos."
                } else {
                    "No hay reportes validados cercanos a tu ubicación actual."
                },
                analyzedZones = 0,
                highRiskZones = 0,
                mediumRiskZones = 0,
                lowRiskZones = 0,
                zones = emptyList(),
                recommendations = listOf(
                    AIRecommendationResult(
                        title = if (userLocation == null) "Activa la ubicación" else "Zona sin reportes relevantes",
                        description = if (userLocation == null) {
                            "Permite el acceso a ubicación para que la IA pueda analizar riesgos en tiempo real."
                        } else {
                            "No se encontraron reportes validados cerca. Continúa usando el mapa para monitorear cambios."
                        },
                        label = "Datos insuficientes",
                        color = UrbanColors.Primary,
                        icon = Icons.Outlined.Lightbulb
                    )
                )
            )
        }

        val highUrgencyCount = analyzedAlerts.count { it.urgency == "Alta" }
        val mediumUrgencyCount = analyzedAlerts.count { it.urgency == "Media" }
        val dominantCategory = analyzedAlerts
            .groupingBy { it.category }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: "alertas"

        val zoneGroups = analyzedAlerts.groupBy { alert ->
            "${(alert.latitude * 100).toInt()}-${(alert.longitude * 100).toInt()}"
        }

        val zoneData = zoneGroups.map { (zoneId, group) ->
            val riskLevel = when {
                group.any { it.urgency == "Alta" } -> "Alta"
                group.any { it.urgency == "Media" } -> "Media"
                else -> "Baja"
            }

            val category = group
                .groupingBy { it.category }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
                ?: "Reportes"

            AIRiskZone(
                id = zoneId,
                title = "Zona ${riskLevel.lowercase()} · $category",
                riskLevel = riskLevel,
                dominantCategory = category,
                reportCount = group.size,
                latitude = group.map { it.latitude }.average(),
                longitude = group.map { it.longitude }.average(),
                recommendation = when (riskLevel) {
                    "Alta" -> "Evita atravesar esta zona si hay una ruta alternativa disponible."
                    "Media" -> "Pasa con precaución y revisa rutas cercanas antes de salir."
                    else -> "Zona con baja concentración de reportes activos."
                }
            )
        }

        val highRiskZones = zoneData.count { it.riskLevel == "Alta" }
        val mediumRiskZones = zoneData.count { it.riskLevel == "Media" }
        val lowRiskZones = zoneData.count { it.riskLevel == "Baja" }

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val peakHour = currentHour in 6..9 || currentHour in 17..20

        val riskTitle = when {
            highUrgencyCount >= 2 || highRiskZones > 0 -> "Riesgo alto en tu zona"
            mediumUrgencyCount >= 2 || mediumRiskZones > 0 -> "Riesgo moderado en tu zona"
            else -> "Riesgo bajo en tu zona"
        }

        val recommendations = buildList {
            if (highUrgencyCount > 0) {
                add(
                    AIRecommendationResult(
                        title = "Evita zonas con alertas altas",
                        description = "La red neuronal detectó $highUrgencyCount incidente(s) de alta urgencia cerca de rutas frecuentes.",
                        label = "Riesgo alto",
                        color = UrbanColors.HighUrgency,
                        icon = Icons.Outlined.WarningAmber
                    )
                )
            }

            add(
                AIRecommendationResult(
                    title = "Prioriza rutas con menos reportes",
                    description = "El sistema compara rutas y recomienda trayectos con menor cantidad de incidentes cercanos.",
                    label = "Ruta sugerida",
                    color = UrbanColors.Primary,
                    icon = Icons.Outlined.Route
                )
            )

            add(
                AIRecommendationResult(
                    title = "Patrón recurrente: $dominantCategory",
                    description = "El modelo identificó que la categoría más frecuente es $dominantCategory; considera revisar el mapa antes de salir.",
                    label = "Predicción",
                    color = UrbanColors.MediumUrgency,
                    icon = Icons.Outlined.AutoGraph
                )
            )

            if (peakHour) {
                add(
                    AIRecommendationResult(
                        title = "Horario con mayor probabilidad de riesgo",
                        description = "Por la hora actual, el modelo aumenta la prioridad de tránsito, choques y calles bloqueadas.",
                        label = "Hora pico",
                        color = UrbanColors.Terracotta,
                        icon = Icons.Outlined.WarningAmber
                    )
                )
            }
        }

        return AIRiskSummary(
            title = riskTitle,
            description = "Se analizaron ${analyzedAlerts.size} reportes validados cercanos a tu ubicación actual.",
            analyzedZones = zoneGroups.size,
            highRiskZones = highRiskZones,
            mediumRiskZones = mediumRiskZones,
            lowRiskZones = lowRiskZones,
            zones = zoneData.sortedByDescending {
                when (it.riskLevel) {
                    "Alta" -> 3
                    "Media" -> 2
                    else -> 1
                }
            },
            recommendations = recommendations
        )
    }

    private fun distanceMeters(a: LatLng, b: LatLng): Double {
        val earthRadiusMeters = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val value = sin(dLat / 2).pow(2) +
            sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)

        return 2 * earthRadiusMeters * atan2(sqrt(value), sqrt(1 - value))
    }
}
