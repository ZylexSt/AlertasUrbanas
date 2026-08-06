package com.example.alertasurbanas.car

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.data.CarMapSyncRepository
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.ReportRepository
import com.example.alertasurbanas.data.UrbanAlert
import com.example.alertasurbanas.model.UrbanReport
import com.example.alertasurbanas.ui.screens.shared.MapTilerMap
import com.example.alertasurbanas.ui.theme.UrbanColors
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun AutomotiveMapDashboardScreen() {
    val repository = remember { ReportRepository() }
    val carMapSyncRepository = remember { CarMapSyncRepository() }
    val scope = rememberCoroutineScope()
    val approvedReports by repository.observeApprovedReports().collectAsState(initial = emptyList())
    val syncedRoute by carMapSyncRepository.observeRoute().collectAsState(initial = com.example.alertasurbanas.data.CarSyncedRoute())
    val alerts = remember(approvedReports) { approvedReports.toUrbanAlerts() }
    val currentLocation = MapDefaults.UtcjLocation

    val selectedCategory = syncedRoute.selectedCategory.ifBlank { "Todas" }
    val selectedUrgency = syncedRoute.selectedUrgency.ifBlank { "Todas" }
    var selectedAlert by remember { mutableStateOf<UrbanAlert?>(null) }
    var showAlertsPanel by remember { mutableStateOf(false) }

    val filteredAlerts = remember(alerts, selectedCategory, selectedUrgency) {
        alerts.filter { alert ->
            val categoryMatch = matchesAutomotiveCategoryFilter(alert, selectedCategory)
            val urgencyMatch = selectedUrgency == "Todas" ||
                alert.urgency.equals(selectedUrgency, ignoreCase = true)
            categoryMatch && urgencyMatch
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UrbanColors.Background)
    ) {
        MapTilerMap(
            modifier = Modifier.fillMaxSize(),
            center = syncedRoute.destination
                ?: syncedRoute.origin
                ?: currentLocation,
            zoom = if (syncedRoute.routes.isNotEmpty()) 14.5 else 14.0,
            showNativeControls = false,
            alerts = filteredAlerts,
            currentLocation = currentLocation,
            routeLines = syncedRoute.routes,
            selectedRouteIndex = syncedRoute.selectedRouteIndex,
            onAlertSelected = {
                selectedAlert = it
            }
        )

        AutomotiveTopBar(
            totalAlerts = filteredAlerts.size,
            selectedCategory = selectedCategory,
            selectedUrgency = selectedUrgency,
            showAlertsPanel = showAlertsPanel,
            onCategoryChange = {
                val nextCategory = if (selectedCategory == it) "Todas" else it
                scope.launch {
                    carMapSyncRepository.publishFilters(
                        selectedCategory = nextCategory,
                        selectedUrgency = selectedUrgency,
                        source = "automotive"
                    )
                }
            },
            onUrgencyChange = {
                val nextUrgency = if (selectedUrgency == it) "Todas" else it
                scope.launch {
                    carMapSyncRepository.publishFilters(
                        selectedCategory = selectedCategory,
                        selectedUrgency = nextUrgency,
                        source = "automotive"
                    )
                }
            },
            onToggleAlertsPanel = { showAlertsPanel = !showAlertsPanel },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        if (syncedRoute.routes.isNotEmpty()) {
            AutomotiveRouteActiveCard(
                destinationName = syncedRoute.destinationName.ifBlank { "Destino seleccionado" },
                routeSummaries = syncedRoute.routeSummaries,
                routeAlerts = syncedRoute.nearbyReports,
                selectedRouteIndex = syncedRoute.selectedRouteIndex,
                onCancelRoute = {
                    scope.launch {
                        carMapSyncRepository.clearRoute(source = "automotive")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 118.dp)
                    .width(360.dp)
            )
        } else {
            WaitingForMobileRouteCard(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, bottom = 118.dp)
                    .width(360.dp)
            )
        }

        if (showAlertsPanel) {
            NearbyAlertsPanel(
                alerts = filteredAlerts.take(5),
                selectedAlertId = selectedAlert?.id,
                onClose = { showAlertsPanel = false },
                onSelectAlert = {
                    selectedAlert = it
                    showAlertsPanel = false
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 96.dp, end = 28.dp, bottom = 112.dp)
                    .width(430.dp)
                    .fillMaxHeight()
            )
        }

        AutomotiveBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun AutomotiveTopBar(
    totalAlerts: Int,
    selectedCategory: String,
    selectedUrgency: String,
    showAlertsPanel: Boolean,
    onCategoryChange: (String) -> Unit,
    onUrgencyChange: (String) -> Unit,
    onToggleAlertsPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(76.dp)
            .background(Color.White.copy(alpha = 0.96f))
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.GridView,
            contentDescription = null,
            tint = UrbanColors.TextPrimary,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(22.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(UrbanColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column {
            Text(
                text = "Mapa de alertas",
                color = UrbanColors.TextPrimary,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$totalAlerts alertas sincronizadas",
                color = UrbanColors.TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        AutomotiveChip(
            text = "Tránsito",
            icon = Icons.Outlined.DirectionsCar,
            selected = selectedCategory == "Tránsito",
            onClick = { onCategoryChange("Tránsito") }
        )

        Spacer(modifier = Modifier.width(12.dp))

        AutomotiveChip(
            text = "Vía pública",
            icon = Icons.Outlined.Construction,
            selected = selectedCategory == "Vía pública",
            onClick = { onCategoryChange("Vía pública") }
        )

        Spacer(modifier = Modifier.width(12.dp))

        AutomotiveChip(
            text = "Alertas",
            icon = Icons.Outlined.NotificationsNone,
            selected = showAlertsPanel,
            onClick = onToggleAlertsPanel
        )

        Spacer(modifier = Modifier.width(12.dp))

        AutomotiveChip(
            text = "Riesgo alto",
            icon = Icons.Outlined.ReportProblem,
            selected = selectedUrgency.equals("Alta", ignoreCase = true),
            alertColor = UrbanColors.HighUrgency,
            onClick = { onUrgencyChange("Alta") }
        )
    }
}

@Composable
private fun AutomotiveChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    alertColor: Color = UrbanColors.Primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) alertColor else Color.White.copy(alpha = 0.94f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else UrbanColors.TextPrimary,
            modifier = Modifier.size(23.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            color = if (selected) Color.White else UrbanColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NearbyAlertsPanel(
    alerts: List<UrbanAlert>,
    selectedAlertId: String?,
    onClose: () -> Unit,
    onSelectAlert: (UrbanAlert) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = null,
                    tint = UrbanColors.Primary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Alertas cercanas",
                    color = UrbanColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = UrbanColors.TextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = UrbanColors.Border)

            if (alerts.isEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "No hay alertas aprobadas para mostrar.",
                    color = UrbanColors.TextSecondary,
                    fontSize = 18.sp
                )
            } else {
                alerts.forEach { alert ->
                    AutomotiveAlertRow(
                        alert = alert,
                        selected = alert.id == selectedAlertId,
                        onClick = { onSelectAlert(alert) }
                    )
                    Divider(color = UrbanColors.Border)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Datos en vivo desde Firebase",
                color = UrbanColors.Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AutomotiveAlertRow(
    alert: UrbanAlert,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = urgencyColor(alert.urgency)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) UrbanColors.Primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForCategory(alert.category),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                color = UrbanColors.TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = alert.address,
                color = UrbanColors.TextSecondary,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = alert.urgency,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = alert.distanceText,
            color = UrbanColors.TextSecondary,
            fontSize = 15.sp
        )

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = UrbanColors.TextSecondary
        )
    }
}

@Composable
private fun AlertPreviewCard(
    alert: UrbanAlert,
    modifier: Modifier = Modifier
) {
    val color = urgencyColor(alert.urgency)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForCategory(alert.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        color = UrbanColors.TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = UrbanColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = alert.address,
                            color = UrbanColors.TextSecondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = alert.urgency,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Selecciona una ruta desde el mapa de la app móvil para verla aquí.",
                color = UrbanColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AutomotiveRouteActiveCard(
    destinationName: String,
    routeSummaries: List<String>,
    routeAlerts: List<Int>,
    selectedRouteIndex: Int,
    onCancelRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(UrbanColors.Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null,
                        tint = UrbanColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ruta activa",
                        color = UrbanColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = destinationName,
                        color = UrbanColors.TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onCancelRoute) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Detener ruta",
                        tint = UrbanColors.HighUrgency,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            routeSummaries.take(3).forEachIndexed { index, summary ->
                SyncedRouteRow(
                    index = index,
                    summary = summary,
                    nearbyReports = routeAlerts.getOrNull(index) ?: 0,
                    selected = index == selectedRouteIndex
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sincronizada desde la app móvil",
                color = UrbanColors.Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WaitingForMobileRouteCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(UrbanColors.Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = UrbanColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Esperando ruta",
                    color = UrbanColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Selecciona una ruta en la app móvil para verla aquí.",
                    color = UrbanColors.TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SyncedRouteRow(
    index: Int,
    summary: String,
    nearbyReports: Int,
    selected: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) UrbanColors.Primary.copy(alpha = 0.12f) else UrbanColors.Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ruta ${index + 1}",
            color = if (selected) UrbanColors.Primary else UrbanColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$summary · $nearbyReports alertas",
            color = UrbanColors.TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AutomotiveBottomBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(78.dp)
            .background(Color.Black.copy(alpha = 0.88f))
            .padding(horizontal = 38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "◉", color = Color.White, fontSize = 32.sp)
        Spacer(modifier = Modifier.width(42.dp))
        Text(text = "Alertas", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(30.dp))
        Text(text = "Ruta segura", color = Color.White.copy(alpha = 0.75f), fontSize = 18.sp)
        Spacer(modifier = Modifier.width(30.dp))
        Text(text = "Mapa", color = Color.White.copy(alpha = 0.75f), fontSize = 18.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "En vivo", color = UrbanColors.PrimaryLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private fun List<UrbanReport>.toUrbanAlerts(): List<UrbanAlert> {
    return mapNotNull { report ->
        val latitude = report.latitude ?: return@mapNotNull null
        val longitude = report.longitude ?: return@mapNotNull null
        val coordinate = LatLng(latitude, longitude)

        UrbanAlert(
            id = report.id,
            title = report.type.ifBlank { "Alerta urbana" },
            category = report.type,
            address = report.locationName.ifBlank { "Ubicación reportada" },
            description = report.description,
            urgency = report.urgency.ifBlank { "Media" },
            distanceText = distanceText(MapDefaults.UtcjLocation, coordinate),
            timeText = timeAgo(report.createdAt),
            latitude = latitude,
            longitude = longitude
        )
    }
}

private fun iconForCategory(category: String): ImageVector {
    val normalized = category.lowercase()
    return when {
        "bache" in normalized || "vía" in normalized || "via" in normalized -> Icons.Outlined.Construction
        "luminaria" in normalized || "ilumin" in normalized -> Icons.Outlined.Lightbulb
        "residuo" in normalized || "basura" in normalized -> Icons.Outlined.Delete
        "incendio" in normalized || "fuego" in normalized -> Icons.Outlined.LocalFireDepartment
        "choque" in normalized || "tránsito" in normalized || "transito" in normalized || "veh" in normalized -> Icons.Outlined.DirectionsCar
        "bloque" in normalized || "cerrada" in normalized -> Icons.Outlined.Traffic
        "seguridad" in normalized || "riesgo" in normalized -> Icons.Outlined.Security
        else -> Icons.Outlined.ReportProblem
    }
}

private fun String.normalizedCategoryKey(): String {
    val normalized = lowercase()
    return when {
        "choque" in normalized || "tránsito" in normalized || "transito" in normalized || "veh" in normalized -> "transito"
        "bache" in normalized || "vía" in normalized || "via" in normalized || "bloque" in normalized || "residuo" in normalized || "basura" in normalized -> "via_publica"
        else -> "otro"
    }
}

private fun matchesAutomotiveCategoryFilter(alert: UrbanAlert, selectedCategory: String): Boolean {
    if (selectedCategory == "Todas") return true

    val normalized = "${alert.category} ${alert.title} ${alert.description}".lowercase()
    return when (selectedCategory) {
        "Tránsito" -> "choque" in normalized ||
            "tránsito" in normalized ||
            "transito" in normalized ||
            "veh" in normalized
        "Vía pública" -> "bache" in normalized ||
            "vía" in normalized ||
            "via" in normalized ||
            "bloque" in normalized ||
            "cerrada" in normalized ||
            "residuo" in normalized ||
            "basura" in normalized
        else -> alert.category.equals(selectedCategory, ignoreCase = true) ||
            alert.title.equals(selectedCategory, ignoreCase = true)
    }
}

private fun urgencyColor(urgency: String): Color {
    return when {
        urgency.equals("Alta", ignoreCase = true) -> UrbanColors.HighUrgency
        urgency.equals("Media", ignoreCase = true) -> UrbanColors.MediumUrgency
        else -> UrbanColors.Primary
    }
}

private fun distanceText(origin: LatLng, destination: LatLng): String {
    val meters = distanceMeters(origin, destination)
    return if (meters < 1000) {
        "${meters.toInt()} m"
    } else {
        "%.1f km".format(meters / 1000.0)
    }
}

private fun timeAgo(createdAt: Long): String {
    val minutes = ((System.currentTimeMillis() - createdAt) / 60_000L).coerceAtLeast(0L)
    return when {
        minutes < 1 -> "Ahora"
        minutes < 60 -> "Hace $minutes min"
        minutes < 1440 -> "Hace ${minutes / 60} h"
        else -> "Hace ${minutes / 1440} d"
    }
}

private fun countReportsNearRoute(
    routePoints: List<LatLng>,
    alerts: List<UrbanAlert>
): Int {
    if (routePoints.isEmpty()) return 0

    return alerts.count { alert ->
        val alertPoint = LatLng(alert.latitude, alert.longitude)
        routePoints.any { routePoint -> distanceMeters(routePoint, alertPoint) <= 180.0 }
    }
}

private fun distanceMeters(start: LatLng, end: LatLng): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(end.latitude - start.latitude)
    val dLon = Math.toRadians(end.longitude - start.longitude)
    val lat1 = Math.toRadians(start.latitude)
    val lat2 = Math.toRadians(end.latitude)

    val a = sin(dLat / 2).pow(2.0) +
        cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}
