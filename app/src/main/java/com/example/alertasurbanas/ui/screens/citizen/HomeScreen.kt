package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.model.UrbanReport
import org.maplibre.android.geometry.LatLng
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


private val Background = UrbanColors.Background
private val Primary = UrbanColors.Primary
private val TextPrimary = UrbanColors.TextPrimary
private val Terracotta = UrbanColors.Terracotta
private val HighUrgency = UrbanColors.HighUrgency
private val MediumUrgency = UrbanColors.MediumUrgency

private data class AlertCategory(
    val name: String,
    val icon: ImageVector
)


@Composable
fun HomeScreen(
    userName: String = "Usuario",
    reports: List<UrbanReport> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String = "",
    onOpenReport: (UrbanReport) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val categories = listOf(
        AlertCategory("Todas", Icons.Outlined.GridView),
        AlertCategory("Tránsito", Icons.Outlined.DirectionsCar),
        AlertCategory("Vía pública", Icons.Outlined.Construction),
        AlertCategory("Iluminación", Icons.Outlined.Lightbulb),
        AlertCategory("Seguridad", Icons.Outlined.Shield)
    )

        val distanceOrigin = MapDefaults.UtcjLocation
    val nearbyReports = reports
        .filter { it.latitude != null && it.longitude != null }
        .sortedBy { report ->
            haversineMeters(distanceOrigin, LatLng(report.latitude ?: 0.0, report.longitude ?: 0.0))
        }
        .take(2)

    Scaffold(
        containerColor = Background,
        bottomBar = { UrbanBottomBar(selectedItem = "Inicio",
            onItemSelected = onNavigate
        ) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header(userName = userName) }
            item { SearchBar() }
            item { NearbyAlert(report = nearbyReports.firstOrNull(), reference = distanceOrigin) }

            item {
                AIHomeBanner(
                    onClick = {
                        onNavigate("IA")
                    }
                )
            }

            item {
                Text(
                    text = "Categorías",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            selected = category.name == "Todas"
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertas cerca de ti",
                        modifier = Modifier.weight(1f),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    TextButton(
                        onClick = {
                            onNavigate("Alertas")
                        }
                    ) {
                        Text(
                            text = "Ver todas",
                            color = Primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = HighUrgency,
                        fontSize = 13.sp
                    )
                }
            } else if (nearbyReports.isEmpty()) {
                item {
                    Text(
                        text = "Aun no hay alertas validadas cerca de ti.",
                        color = TextPrimary.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
            } else {
                items(nearbyReports, key = { it.id }) { report ->
                    AlertCard(
                        report = report,
                        onClick = { onOpenReport(report) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(userName: String) {
    val greetingName = userName
        .trim()
        .ifBlank { "Usuario" }
        .split(" ")
        .firstOrNull()
        .orEmpty()
        .ifBlank { "Usuario" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "¡Hola, $greetingName!",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Gracias por hacer tu ciudad más segura.",
                color = TextPrimary.copy(alpha = 0.65f),
                fontSize = 13.sp
            )
        }

        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notificaciones",
                tint = TextPrimary,
                modifier = Modifier.padding(11.dp)
            )
        }
    }
}

@Composable
private fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        placeholder = {
            Text(
                text = "Buscar una calle o zona",
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Primary,
            unfocusedBorderColor = UrbanColors.BorderSoft
        )
    )
}

@Composable
private fun NearbyAlert(
    report: UrbanReport?,
    reference: LatLng
) {
    val title = report?.type?.ifBlank { "Reporte urbano" } ?: "Sin alertas cercanas"
    val distanceText = report?.let { formatReportDistance(it, reference) } ?: "Aún no hay reportes validados cerca"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Terracotta.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = HighUrgency.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = HighUrgency,
                    modifier = Modifier.padding(9.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alerta cercana",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "$title · $distanceText",
                    fontSize = 12.sp,
                    color = TextPrimary.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: AlertCategory,
    selected: Boolean
) {
    Surface(
        modifier = Modifier.width(82.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Primary else Color.White,
        shadowElevation = if (selected) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 13.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (selected) Color.White else TextPrimary
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = category.name,
                color = if (selected) Color.White else TextPrimary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AlertCard(
    report: UrbanReport,
    onClick: () -> Unit
) {
    val color = urgencyColor(report.urgency)
    val icon = reportIcon(report.type)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.type.ifBlank { "Reporte urbano" },
                        modifier = Modifier.weight(1f),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = color
                    ) {
                        Text(
                            text = report.urgency.ifBlank { "Media" },
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 4.dp
                            ),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AlertInformation(
                    icon = Icons.Outlined.LocationOn,
                    text = report.locationName.ifBlank { "Sin ubicacion" }
                )

                AlertInformation(
                    icon = Icons.Outlined.NearMe,
                    text = formatReportDistance(report, MapDefaults.UtcjLocation)
                )

                AlertInformation(
                    icon = Icons.Outlined.Schedule,
                    text = formatReportDate(report.createdAt)
                )
            }

            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "Guardar alerta",
                tint = TextPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AlertInformation(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            color = TextPrimary.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}
@Composable
fun UrbanBottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == "Inicio",
            onClick = { onItemSelected("Inicio") },
            icon = {
                Icon(Icons.Outlined.Home, contentDescription = null)
            },
            label = { Text("Inicio") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Mapa",
            onClick = { onItemSelected("Mapa") },
            icon = {
                Icon(Icons.Outlined.Map, contentDescription = null)
            },
            label = { Text("Mapa") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Reportar",
            onClick = { onItemSelected("Reportar") },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Primary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(9.dp)
                    )
                }
            },
            label = { Text("Reportar") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Alertas",
            onClick = { onItemSelected("Alertas") },
            icon = {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null
                )
            },
            label = { Text("Alertas") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Perfil",
            onClick = { onItemSelected("Perfil") },
            icon = {
                Icon(
                    Icons.Outlined.PersonOutline,
                    contentDescription = null
                )
            },
            label = { Text("Perfil") },
            colors = navigationColors()
        )
    }
}
@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Primary,
    selectedTextColor = Primary,
    indicatorColor = Primary.copy(alpha = 0.12f),
    unselectedIconColor = TextPrimary.copy(alpha = 0.65f),
    unselectedTextColor = TextPrimary.copy(alpha = 0.65f)
)


private fun reportIcon(type: String): ImageVector {
    val normalizedType = type.lowercase()

    return when {
        normalizedType.contains("luminaria") ||
                normalizedType.contains("iluminacion") ||
                normalizedType.contains("iluminación") -> Icons.Outlined.Lightbulb
        normalizedType.contains("residuo") -> Icons.Outlined.Delete
        normalizedType.contains("transito") ||
                normalizedType.contains("tránsito") -> Icons.Outlined.DirectionsCar
        else -> Icons.Outlined.Construction
    }
}

private fun urgencyColor(urgency: String): Color {
    return when (urgency) {
        "Alta" -> HighUrgency
        "Media" -> MediumUrgency
        "Baja" -> Primary
        else -> MediumUrgency
    }
}

private fun formatReportDate(createdAt: Long): String {
    val elapsedMillis = System.currentTimeMillis() - createdAt

    return when {
        elapsedMillis < TimeUnit.MINUTES.toMillis(1) -> "Hace un momento"
        elapsedMillis < TimeUnit.HOURS.toMillis(1) -> "Hace ${TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)} min"
        elapsedMillis < TimeUnit.DAYS.toMillis(1) -> "Hace ${TimeUnit.MILLISECONDS.toHours(elapsedMillis)} h"
        elapsedMillis < TimeUnit.DAYS.toMillis(7) -> "Hace ${TimeUnit.MILLISECONDS.toDays(elapsedMillis)} dias"
        else -> "Hace mas de una semana"
    }
}

private fun formatReportDistance(report: UrbanReport, reference: LatLng): String {
    val latitude = report.latitude ?: return "Ubicación reportada"
    val longitude = report.longitude ?: return "Ubicación reportada"
    return formatDistance(haversineMeters(reference, LatLng(latitude, longitude)))
}

private fun formatDistance(distanceMeters: Double): String {
    return if (distanceMeters < 1000.0) {
        "A ${distanceMeters.toInt()} m"
    } else {
        "A ${String.format("%.1f", distanceMeters / 1000.0)} km"
    }
}

private fun haversineMeters(from: LatLng, to: LatLng): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val fromLat = Math.toRadians(from.latitude)
    val toLat = Math.toRadians(to.latitude)

    val a = sin(dLat / 2).pow(2.0) + cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadiusMeters * c
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    AlertasUrbanasTheme {
        HomeScreen()
    }
}

@Composable
private fun AIHomeBanner(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Primary
            )

            Spacer(modifier = Modifier.width(11.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recomendaciones inteligentes",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Consulta rutas y zonas de riesgo.",
                    color = TextPrimary.copy(alpha = 0.62f),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TextPrimary
            )
        }
    }
}





