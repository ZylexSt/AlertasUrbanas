package com.example.alertasurbanas.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.model.UrbanReport
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import com.example.alertasurbanas.ui.theme.UrbanColors
import java.util.concurrent.TimeUnit

private val AdminBackground = UrbanColors.Background
private val AdminPrimary = UrbanColors.Primary
private val AdminText = UrbanColors.TextPrimary
private val AdminWarning = UrbanColors.MediumUrgency
private val AdminDanger = UrbanColors.HighUrgency
private val AdminSuccess = UrbanColors.Success

@Composable
fun AdminPanelScreen(
    reports: List<UrbanReport> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String = "",
    onBack: () -> Unit = {},
    onOpenReport: (UrbanReport) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf("Pendientes")
    }

    val filteredReports = reports.filter { report ->
        when (selectedFilter) {
            "Pendientes" -> report.status == "pending"
            "Validados" -> report.status == "approved"
            "Rechazados" -> report.status == "rejected"
            else -> true
        }
    }

    Scaffold(
        containerColor = AdminBackground,
        bottomBar = {
            AdminBottomBar(
                selectedItem = "Panel",
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
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AdminHeader(onBack = onBack)
            }

            item {
                AdminSummary(reports = reports)
            }

            item {
                Text(
                    text = "Gestion de reportes",
                    color = AdminText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(
                        listOf(
                            "Pendientes",
                            "Validados",
                            "Rechazados",
                            "Todos"
                        )
                    ) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                            },
                            label = {
                                Text(filter)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AdminPrimary,
                                selectedLabelColor = Color.White
                            )
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
                        text = if (selectedFilter == "Pendientes") {
                            "Reportes pendientes"
                        } else {
                            selectedFilter
                        },
                        modifier = Modifier.weight(1f),
                        color = AdminText,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${filteredReports.size} reportes",
                        color = AdminText.copy(alpha = 0.6f),
                        fontSize = 12.sp
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
                        CircularProgressIndicator(color = AdminPrimary)
                    }
                }
            } else if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = AdminDanger,
                        fontSize = 13.sp
                    )
                }
            } else if (filteredReports.isEmpty()) {
                item {
                    Text(
                        text = "No hay reportes en esta categoria.",
                        color = AdminText.copy(alpha = 0.62f),
                        fontSize = 13.sp
                    )
                }
            } else {
                items(filteredReports, key = { it.id }) { report ->
                    AdminReportCard(
                        report = report,
                        onOpenReport = { onOpenReport(report) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = AdminText
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Panel de administrador",
                color = AdminText,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Validacion y gestion de incidentes.",
                color = AdminText.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = AdminPrimary.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = AdminPrimary,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Admin",
                    color = AdminPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AdminSummary(reports: List<UrbanReport>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        AdminSummaryCard(
            number = reports.count { it.status == "pending" }.toString(),
            label = "Pendientes",
            color = AdminWarning,
            modifier = Modifier.weight(1f)
        )

        AdminSummaryCard(
            number = reports.count { it.status == "approved" }.toString(),
            label = "Validados",
            color = AdminSuccess,
            modifier = Modifier.weight(1f)
        )

        AdminSummaryCard(
            number = reports.count { it.status == "rejected" }.toString(),
            label = "Rechazados",
            color = AdminDanger,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AdminSummaryCard(
    number: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.11f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                color = color,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = AdminText.copy(alpha = 0.65f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminReportCard(
    report: UrbanReport,
    onOpenReport: () -> Unit
) {
    val urgencyColor = urgencyColor(report.urgency)

    Card(
        onClick = onOpenReport,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = urgencyColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = reportIcon(report.type),
                            contentDescription = null,
                            tint = urgencyColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = report.type.ifBlank { "Reporte urbano" },
                            modifier = Modifier.weight(1f),
                            color = AdminText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = urgencyColor
                        ) {
                            Text(
                                text = report.urgency.ifBlank { "Media" },
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AdminInformation(
                        icon = Icons.Outlined.LocationOn,
                        text = report.locationName.ifBlank { "Sin ubicacion" }
                    )

                    AdminInformation(
                        icon = Icons.Outlined.Person,
                        text = "Por ${report.userName.ifBlank { "Usuario ciudadano" }}"
                    )

                    AdminInformation(
                        icon = Icons.Outlined.Schedule,
                        text = formatReportDate(report.createdAt)
                    )

                    AdminInformation(
                        icon = Icons.Outlined.Info,
                        text = reportStatusLabel(report.status)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver detalle",
                    tint = AdminPrimary
                )
            }
        }
    }
}

@Composable
private fun AdminInformation(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AdminPrimary,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = AdminText.copy(alpha = 0.63f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun reportIcon(type: String): ImageVector {
    val normalizedType = type.lowercase()

    return when {
        normalizedType.contains("luminaria") ||
                normalizedType.contains("iluminacion") ||
                normalizedType.contains("iluminación") -> Icons.Outlined.Lightbulb
        normalizedType.contains("residuo") -> Icons.Outlined.Delete
        normalizedType.contains("transito") ||
                normalizedType.contains("tránsito") -> Icons.Outlined.DirectionsCar
        normalizedType.contains("bloque") || normalizedType.contains("calle") -> Icons.Outlined.Traffic
        else -> Icons.Outlined.Construction
    }
}

private fun urgencyColor(urgency: String): Color {
    return when (urgency) {
        "Alta" -> AdminDanger
        "Media" -> AdminWarning
        "Baja" -> AdminPrimary
        else -> AdminWarning
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminPanelPreview() {
    AlertasUrbanasTheme {
        AdminPanelScreen()
    }
}