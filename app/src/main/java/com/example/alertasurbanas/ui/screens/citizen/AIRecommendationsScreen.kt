package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.data.AIRecommendationEngine
import com.example.alertasurbanas.data.AIRecommendationApi
import com.example.alertasurbanas.data.AIRecommendationResult
import com.example.alertasurbanas.data.AIRiskZone
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.MapRiskZone
import com.example.alertasurbanas.data.MapSearchService
import com.example.alertasurbanas.data.ReportRepository
import com.example.alertasurbanas.data.UrbanAlert
import com.example.alertasurbanas.model.UrbanReport
import com.example.alertasurbanas.ui.screens.shared.MapTilerMap
import com.example.alertasurbanas.ui.screens.shared.rememberLiveUserLocation
import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val AIBackground = UrbanColors.Background
private val AIPrimary = UrbanColors.Primary
private val AIText = UrbanColors.TextPrimary
private val AIOrange = UrbanColors.Terracotta
private const val AI_ANALYSIS_RADIUS_METERS = 15_000.0

private data class SaferStreetSuggestion(
    val name: String,
    val coordinate: LatLng,
    val nearbyAlertCount: Int,
    val highestRisk: String
)

@Composable
fun AIRecommendationsScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    reports: List<UrbanReport> = emptyList(),
    alerts: List<UrbanAlert> = emptyList()
) {
    val currentLocation by rememberLiveUserLocation(
        autoRequestPermission = true,
        intervalMillis = 6_000L,
        minUpdateDistanceMeters = 6f
    )

    val reportRepository = remember { ReportRepository() }
    val approvedReportsFlow = remember { reportRepository.observeApprovedReports() }
    val realtimeApprovedReports by approvedReportsFlow.collectAsState(initial = reports)

    val approvedReports = remember(realtimeApprovedReports, reports) {
        val source = if (realtimeApprovedReports.isNotEmpty()) realtimeApprovedReports else reports
        source.filter { report ->
            report.status == "approved" &&
                report.latitude != null &&
                report.longitude != null
        }
    }

    val liveAlerts = remember(approvedReports, alerts) {
        if (approvedReports.isNotEmpty()) approvedReports.toAIAlerts() else alerts
    }

    val analysisAlerts = remember(liveAlerts, currentLocation) {
        val origin = currentLocation ?: return@remember emptyList()
        liveAlerts.filter { alert ->
            distanceMeters(
                a = origin,
                b = LatLng(alert.latitude, alert.longitude)
            ) <= AI_ANALYSIS_RADIUS_METERS
        }
    }

    val analysisReports = remember(approvedReports, currentLocation) {
        val origin = currentLocation ?: return@remember emptyList()
        approvedReports.filter { report ->
            distanceMeters(
                a = origin,
                b = LatLng(report.latitude ?: 0.0, report.longitude ?: 0.0)
            ) <= AI_ANALYSIS_RADIUS_METERS
        }
    }

    var isUsingRemoteAI by rememberSaveable {
        mutableStateOf(false)
    }

    var isLoadingRemoteAI by rememberSaveable {
        mutableStateOf(false)
    }

    var riskSummary by remember(analysisAlerts, currentLocation) {
        mutableStateOf(
            AIRecommendationEngine.analyze(
                alerts = analysisAlerts,
                userLocation = currentLocation,
                radiusMeters = AI_ANALYSIS_RADIUS_METERS
            )
        )
    }

    var selectedZoneId by rememberSaveable {
        mutableStateOf("")
    }

    val selectedZone = remember(riskSummary.zones, selectedZoneId) {
        riskSummary.zones.firstOrNull { it.id == selectedZoneId }
    }

    val streetReferenceZone = remember(selectedZone, riskSummary.zones, currentLocation, analysisAlerts) {
        selectedZone
            ?: riskSummary.zones.firstOrNull()
            ?: currentLocation?.let { location ->
                AIRiskZone(
                    id = "current-user-location",
                    title = "Tu ubicación actual",
                    riskLevel = highestRiskLevel(analysisAlerts),
                    dominantCategory = mostRepeatedCategory(analysisAlerts),
                    reportCount = analysisAlerts.size,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    recommendation = if (analysisAlerts.isEmpty()) {
                        "No hay reportes validados dentro del rango cercano."
                    } else {
                        "Se usan las alertas cercanas para buscar calles con menor concentración de reportes."
                    }
                )
            }
    }

    var saferStreets by remember {
        mutableStateOf<List<SaferStreetSuggestion>>(emptyList())
    }

    LaunchedEffect(analysisReports, analysisAlerts, currentLocation) {
        if (currentLocation == null) {
            riskSummary = AIRecommendationEngine.analyze(emptyList(), currentLocation)
            isUsingRemoteAI = false
            return@LaunchedEffect
        }

        if (analysisReports.isEmpty()) {
            riskSummary = AIRecommendationEngine.analyze(
                alerts = emptyList(),
                userLocation = currentLocation,
                radiusMeters = AI_ANALYSIS_RADIUS_METERS
            )
            isUsingRemoteAI = false
            return@LaunchedEffect
        }

        isLoadingRemoteAI = true
        val remoteSummary = AIRecommendationApi.getRecommendations(
            reports = analysisReports,
            userLocation = currentLocation,
            analysisRadiusMeters = AI_ANALYSIS_RADIUS_METERS
        )
        isLoadingRemoteAI = false

        if (remoteSummary != null) {
            riskSummary = sanitizeSummaryForCurrentArea(
                summary = remoteSummary,
                alerts = analysisAlerts,
                userLocation = currentLocation
            )
            isUsingRemoteAI = true
        } else {
            riskSummary = AIRecommendationEngine.analyze(
                alerts = analysisAlerts,
                userLocation = currentLocation,
                radiusMeters = AI_ANALYSIS_RADIUS_METERS
            )
            isUsingRemoteAI = false
        }
    }

    LaunchedEffect(streetReferenceZone?.id, analysisAlerts) {
        val zone = streetReferenceZone ?: return@LaunchedEffect
        saferStreets = emptyList()

        saferStreets = findSaferNearbyStreets(
            zone = zone,
            alerts = analysisAlerts
        )
    }

    Scaffold(
        containerColor = AIBackground,
        bottomBar = {
            UrbanBottomBar(
                selectedItem = "IA",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AIHeader(onBack = onBack)
            }

            item {
                AIHeroCard(
                    title = riskSummary.title,
                    description = when {
                        isLoadingRemoteAI -> "Consultando la red neuronal en Python con reportes recientes..."
                        isUsingRemoteAI -> "${riskSummary.description} · IA conectada"
                        else -> "${riskSummary.description} · modo local"
                    }
                )
            }

            item {
                Text(
                    text = "Análisis de tu zona",
                    color = AIText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                AIRiskMapCard(
                    zones = riskSummary.zones,
                    alerts = analysisAlerts,
                    selectedZone = selectedZone,
                    streetReferenceZone = streetReferenceZone,
                    hasLocation = currentLocation != null,
                    saferStreets = saferStreets,
                    currentLocation = currentLocation,
                    onSelectZone = { zone ->
                        selectedZoneId = zone.id
                    },
                    analyzedZones = riskSummary.analyzedZones,
                    highRiskZones = riskSummary.highRiskZones,
                    mediumRiskZones = riskSummary.mediumRiskZones,
                    lowRiskZones = riskSummary.lowRiskZones
                )
            }

            item {
                Text(
                    text = "Recomendaciones para ti",
                    color = AIText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(riskSummary.recommendations) {
                AIRecommendationCard(it)
            }

            item {
                AIInformationCard()
            }
        }
    }
}

@Composable
private fun AIHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = AIText
            )
        }

        Column {
            Text(
                text = "Recomendaciones de IA",
                color = AIText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Análisis inteligente de riesgos urbanos.",
                color = AIText.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AIHeroCard(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        AIPrimary,
                        UrbanColors.PrimaryLight
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(11.dp)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.83f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun AIRiskMapCard(
    zones: List<AIRiskZone>,
    alerts: List<UrbanAlert>,
    selectedZone: AIRiskZone?,
    streetReferenceZone: AIRiskZone?,
    saferStreets: List<SaferStreetSuggestion>,
    currentLocation: LatLng?,
    hasLocation: Boolean,
    onSelectZone: (AIRiskZone) -> Unit,
    analyzedZones: Int,
    highRiskZones: Int,
    mediumRiskZones: Int,
    lowRiskZones: Int
) {
    val zoneAlerts = remember(zones) {
        zones.map { zone ->
            UrbanAlert(
                id = "ai-zone-${zone.id}",
                title = zone.title,
                category = zone.dominantCategory,
                address = "${zone.reportCount} reporte(s) detectado(s)",
                description = zone.recommendation,
                urgency = zone.riskLevel,
                distanceText = "Zona IA",
                timeText = "Actualizado ahora",
                latitude = zone.latitude,
                longitude = zone.longitude
            )
        }
    }

    val mapCenter = currentLocation ?: MapDefaults.UtcjLocation

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                MapTilerMap(
                    modifier = Modifier.fillMaxSize(),
                    center = mapCenter,
                    zoom = 14.0,
                    alerts = zoneAlerts,
                    currentLocation = currentLocation,
                    riskZones = zones.map { zone ->
                        MapRiskZone(
                            id = zone.id,
                            latitude = zone.latitude,
                            longitude = zone.longitude,
                            riskLevel = zone.riskLevel,
                            reportCount = zone.reportCount,
                            radiusMeters = when (zone.riskLevel) {
                                "Alta" -> 390.0
                                "Media" -> 320.0
                                else -> 260.0
                            } + (zone.reportCount * 28.0)
                        )
                    },
                    onAlertSelected = { alert ->
                        zones.firstOrNull { "ai-zone-${it.id}" == alert.id }?.let(onSelectZone)
                    }
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = Color.White.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = AIPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "Mapa IA",
                            color = AIText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = AIPrimary
                )

                Spacer(modifier = Modifier.width(9.dp))

                Column {
                    Text(
                        text = when {
                            !hasLocation -> "Esperando ubicación"
                            analyzedZones == 0 -> "Sin zonas cercanas"
                            else -> "$analyzedZones zonas analizadas"
                        },
                        color = AIText,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = when {
                            !hasLocation -> "Permite la ubicación para analizar tu zona real"
                            analyzedZones == 0 -> "No hay reportes validados dentro del rango cercano"
                            else -> "$highRiskZones alta(s), $mediumRiskZones media(s), $lowRiskZones baja(s) · actualizado ahora"
                        },
                        color = AIText.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            streetReferenceZone?.let { zone ->
                Divider(color = UrbanColors.Border.copy(alpha = 0.7f))
                AISelectedZoneCard(
                    zone = zone,
                    isCurrentLocationReference = zone.id == "current-user-location",
                    saferStreets = saferStreets
                )
            }
        }
    }
}

@Composable
private fun AISelectedZoneCard(
    zone: AIRiskZone,
    isCurrentLocationReference: Boolean = false,
    saferStreets: List<SaferStreetSuggestion>
) {
    Column(
        modifier = Modifier.padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = riskColor(zone.riskLevel).copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = riskColor(zone.riskLevel),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentLocationReference) "Calles cercanas a tu ubicación" else zone.title,
                    color = AIText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = zone.recommendation,
                    color = AIText.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Text(
                    text = if (isCurrentLocationReference) {
                        "${zone.reportCount} reporte(s) en el rango · ${zone.dominantCategory}"
                    } else {
                        "${zone.reportCount} reporte(s) · ${zone.dominantCategory}"
                    },
                    color = riskColor(zone.riskLevel),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (saferStreets.isNotEmpty()) {
            Text(
                text = "Calles o sectores cercanos con menos alertas",
                color = AIText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            saferStreets.take(3).forEach { street ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = UrbanColors.NeutralPanel,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = riskColor(street.highestRisk).copy(alpha = 0.13f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = riskColor(street.highestRisk),
                            modifier = Modifier.padding(8.dp).size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = street.name,
                            color = AIText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Text(
                            text = if (street.nearbyAlertCount == 0) {
                                "Sin alertas cercanas detectadas"
                            } else {
                                "${street.nearbyAlertCount} alerta(s) cercana(s) · riesgo ${street.highestRisk.lowercase()}"
                            },
                            color = AIText.copy(alpha = 0.62f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Buscando calles cercanas con menos alertas...",
                color = AIText.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
        }

        Text(
            text = "Para calcular una ruta segura, usa el mapa, elige destino y compara recorridos completos contra las alertas activas.",
            color = AIText.copy(alpha = 0.62f),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun AIRecommendationCard(
    recommendation: AIRecommendationResult
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = recommendation.color.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = recommendation.icon,
                    contentDescription = null,
                    tint = recommendation.color,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    color = AIText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = recommendation.description,
                    color = AIText.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = recommendation.label,
                    color = recommendation.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AIInformationCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AIOrange.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AIOrange
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Estas recomendaciones son orientativas y se generan a partir de reportes ciudadanos. Mantén atención a las condiciones reales del camino.",
                color = AIText.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

private fun List<UrbanReport>.toAIAlerts(): List<UrbanAlert> {
    return filter { report ->
        report.latitude != null && report.longitude != null
    }.map { report ->
        UrbanAlert(
            id = report.id,
            title = report.type.ifBlank { "Reporte urbano" },
            category = report.type.ifBlank { "Reporte" },
            address = report.locationName.ifBlank { "Sin ubicación" },
            description = report.description,
            urgency = report.urgency.ifBlank { "Media" },
            distanceText = "Reporte real",
            timeText = "Analizado por IA",
            latitude = report.latitude ?: 0.0,
            longitude = report.longitude ?: 0.0
        )
    }
}

private fun riskColor(riskLevel: String): Color {
    return when (riskLevel) {
        "Alta" -> UrbanColors.HighUrgency
        "Media" -> UrbanColors.MediumUrgency
        else -> UrbanColors.Primary
    }
}

private fun sanitizeSummaryForCurrentArea(
    summary: com.example.alertasurbanas.data.AIRiskSummary,
    alerts: List<UrbanAlert>,
    userLocation: LatLng?
): com.example.alertasurbanas.data.AIRiskSummary {
    if (userLocation == null) {
        return AIRecommendationEngine.analyze(emptyList(), null)
    }

    if (alerts.isEmpty()) {
        return AIRecommendationEngine.analyze(
            alerts = emptyList(),
            userLocation = userLocation,
            radiusMeters = AI_ANALYSIS_RADIUS_METERS
        )
    }

    val highestActualRisk = highestRiskLevel(alerts)
    val sanitizedZones = summary.zones.map { zone ->
        zone.copy(
            riskLevel = clampRiskLevel(zone.riskLevel, highestActualRisk),
            title = "Zona ${clampRiskLevel(zone.riskLevel, highestActualRisk).lowercase()} · ${zone.dominantCategory}"
        )
    }

    val highZones = sanitizedZones.count { it.riskLevel == "Alta" }
    val mediumZones = sanitizedZones.count { it.riskLevel == "Media" }
    val lowZones = sanitizedZones.count { it.riskLevel == "Baja" }

    val title = when (highestActualRisk) {
        "Alta" -> "Riesgo alto en tu zona"
        "Media" -> "Riesgo moderado en tu zona"
        else -> "Riesgo bajo en tu zona"
    }

    return summary.copy(
        title = title,
        description = "Se analizaron ${alerts.size} reporte(s) validado(s) dentro de 15 km de tu ubicación actual.",
        highRiskZones = highZones,
        mediumRiskZones = mediumZones,
        lowRiskZones = lowZones,
        zones = sanitizedZones
    )
}

private fun clampRiskLevel(value: String, maxRiskLevel: String): String {
    return if (riskPriority(value) > riskPriority(maxRiskLevel)) maxRiskLevel else value
}

private fun mostRepeatedCategory(alerts: List<UrbanAlert>): String {
    return alerts
        .groupingBy { it.category.ifBlank { "Sin reportes cercanos" } }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
        ?: "Sin reportes cercanos"
}

private suspend fun findSaferNearbyStreets(
    zone: AIRiskZone,
    alerts: List<UrbanAlert>
): List<SaferStreetSuggestion> {
    val center = LatLng(zone.latitude, zone.longitude)
    val candidatePoints = listOf(
        offsetCoordinate(center, northMeters = 620.0, eastMeters = 0.0),
        offsetCoordinate(center, northMeters = -620.0, eastMeters = 0.0),
        offsetCoordinate(center, northMeters = 0.0, eastMeters = 620.0),
        offsetCoordinate(center, northMeters = 0.0, eastMeters = -620.0),
        offsetCoordinate(center, northMeters = 440.0, eastMeters = 440.0),
        offsetCoordinate(center, northMeters = 440.0, eastMeters = -440.0),
        offsetCoordinate(center, northMeters = -440.0, eastMeters = 440.0),
        offsetCoordinate(center, northMeters = -440.0, eastMeters = -440.0)
    )

    return candidatePoints.map { point ->
        val nearbyAlerts = alerts.filter { alert ->
            distanceMeters(
                a = point,
                b = LatLng(alert.latitude, alert.longitude)
            ) <= 360.0
        }

        val address = MapSearchService.reverseGeocode(point)
            ?.substringBefore(", México")
            ?.substringBefore(", Mexico")
            ?.ifBlank { null }
            ?: "Calle cercana ${candidatePoints.indexOf(point) + 1}"

        SaferStreetSuggestion(
            name = address,
            coordinate = point,
            nearbyAlertCount = nearbyAlerts.size,
            highestRisk = highestRiskLevel(nearbyAlerts)
        )
    }
        .distinctBy { it.name.lowercase() }
        .sortedWith(
            compareBy<SaferStreetSuggestion> { it.nearbyAlertCount }
                .thenBy { riskPriority(it.highestRisk) }
                .thenBy { it.name }
        )
        .take(4)
}

private fun offsetCoordinate(
    origin: LatLng,
    northMeters: Double,
    eastMeters: Double
): LatLng {
    val earthRadiusMeters = 6_371_000.0
    val newLatitude = origin.latitude + Math.toDegrees(northMeters / earthRadiusMeters)
    val newLongitude = origin.longitude + Math.toDegrees(
        eastMeters / (earthRadiusMeters * cos(Math.toRadians(origin.latitude)))
    )

    return LatLng(newLatitude, newLongitude)
}

private fun highestRiskLevel(alerts: List<UrbanAlert>): String {
    return alerts.maxByOrNull { riskPriority(it.urgency) }?.urgency ?: "Baja"
}

private fun riskPriority(riskLevel: String): Int {
    return when (riskLevel) {
        "Alta" -> 3
        "Media" -> 2
        else -> 1
    }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AIRecommendationsPreview() {
    AlertasUrbanasTheme {
        AIRecommendationsScreen()
    }
}




