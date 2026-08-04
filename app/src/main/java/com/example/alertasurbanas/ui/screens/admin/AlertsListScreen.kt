package com.example.alertasurbanas.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.model.UrbanReport
import com.example.alertasurbanas.ui.theme.UrbanColors
import java.util.concurrent.TimeUnit

private val AlertsBackground = UrbanColors.Background
private val AlertsPrimary = UrbanColors.Primary
private val AlertsTextPrimary = UrbanColors.TextPrimary
private val AlertsTextSecondary = UrbanColors.TextSecondary
private val AlertsHigh = UrbanColors.HighUrgency
private val AlertsMedium = UrbanColors.MediumUrgency
private val AlertsLow = UrbanColors.LowUrgency

@Composable
fun AlertsListScreen(
    reports: List<UrbanReport> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String = "",
    onBack: () -> Unit = {},
    onOpenDetail: (UrbanReport) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    var search by remember { mutableStateOf("") }

    val filteredAlerts = reports.filter { report ->
        val matchesFilter = selectedFilter == "Todas" || report.urgency == selectedFilter
        val matchesSearch =
            search.isBlank() ||
                    report.type.contains(search, ignoreCase = true) ||
                    report.locationName.contains(search, ignoreCase = true) ||
                    report.description.contains(search, ignoreCase = true) ||
                    report.userName.contains(search, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    Scaffold(
        containerColor = AlertsBackground,
        bottomBar = {
            AdminBottomBar(
                selectedItem = "Reportes",
                onItemSelected = onNavigate
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 22.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = AlertsTextPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Alertas",
                            color = AlertsTextPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Incidentes reportados en la ciudad.",
                            color = AlertsTextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Buscar por calle, zona o incidente",
                            color = AlertsTextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = AlertsTextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AlertsPrimary,
                        unfocusedBorderColor = UrbanColors.Border,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listOf("Todas", "Alta", "Media", "Baja")) { filter ->
                        FilterChip(
                            label = filter,
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Reportes activos",
                        color = AlertsTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "${filteredAlerts.size} encontrados",
                        color = AlertsPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AlertsPrimary)
                    }
                }
            } else if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = AlertsHigh,
                        fontSize = 13.sp
                    )
                }
            } else if (filteredAlerts.isEmpty()) {
                item {
                    Text(
                        text = "No hay reportes para mostrar.",
                        color = AlertsTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                items(filteredAlerts, key = { it.id }) { report ->
                    PublicAlertCard(
                        report = report,
                        onClick = { onOpenDetail(report) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (selected) AlertsPrimary else UrbanColors.BorderStrong
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) AlertsPrimary else Color.Transparent
        )
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else AlertsTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PublicAlertCard(
    report: UrbanReport,
    onClick: () -> Unit
) {
    val color = urgencyColor(report.urgency)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(
                        color = color.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reportIcon(report.type),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = report.type.ifBlank { "Reporte urbano" },
                        color = AlertsTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                color = color.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = report.urgency.ifBlank { "Media" },
                            color = color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reportStatusLabel(report.status),
                    color = AlertsPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = AlertsPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = report.locationName.ifBlank { "Sin ubicacion" },
                        color = AlertsTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "${report.userName.ifBlank { "Usuario ciudadano" }} - ${formatReportDate(report.createdAt)}",
                    color = AlertsTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = AlertsTextSecondary
            )
        }
    }
}

private fun reportIcon(type: String): ImageVector {
    val normalizedType = type.lowercase()
    return when {
        normalizedType.contains("luminaria") || normalizedType.contains("iluminacion") || normalizedType.contains("iluminación") -> Icons.Outlined.Lightbulb
        normalizedType.contains("residuo") -> Icons.Outlined.Delete
        normalizedType.contains("transito") || normalizedType.contains("tránsito") -> Icons.Outlined.DirectionsCar
        normalizedType.contains("bloque") || normalizedType.contains("calle") -> Icons.Outlined.Traffic
        else -> Icons.Outlined.Construction
    }
}

private fun urgencyColor(urgency: String): Color {
    return when (urgency) {
        "Alta" -> AlertsHigh
        "Media" -> AlertsMedium
        "Baja" -> AlertsLow
        else -> AlertsMedium
    }
}

private fun reportStatusLabel(status: String): String {
    return when (status) {
        "pending" -> "Pendiente"
        "approved" -> "Validado"
        "rejected" -> "Rechazado"
        else -> status.ifBlank { "Pendiente" }
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
