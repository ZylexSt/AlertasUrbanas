package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

import com.example.alertasurbanas.model.UrbanReport
import java.util.concurrent.TimeUnit

private val ReportsBackground = UrbanColors.Background
private val ReportsPrimary = UrbanColors.Primary
private val ReportsText = UrbanColors.TextPrimary
private val PendingColor = UrbanColors.MediumUrgency
private val ApprovedColor = UrbanColors.Success
private val RejectedColor = UrbanColors.HighUrgency

private data class UserReport(
    val title: String,
    val location: String,
    val date: String,
    val status: String,
    val statusColor: Color,
    val icon: ImageVector
)

@Composable
fun MyReportsScreen(
    reports: List<UrbanReport> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String = "",
    onOpenReport: (UrbanReport) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {

    var selectedFilter by rememberSaveable {
        mutableStateOf("Todos")
    }

    /*
    val reports = listOf(
        UserReport(
            title = "Bache peligroso",
            location = "Av. Siempre Viva 742",
            date = "Hoy, 12:30 p. m.",
            status = "Pendiente",
            statusColor = PendingColor,
            icon = Icons.Outlined.Construction
        ),
        UserReport(
            title = "Luminaria apagada",
            location = "Calle 26 Norte 115",
            date = "Ayer, 8:15 p. m.",
            status = "Validado",
            statusColor = ApprovedColor,
            icon = Icons.Outlined.Lightbulb
        ),
        UserReport(
            title = "Vehículo obstruyendo",
            location = "Av. Reforma 1200",
            date = "15 de junio, 4:10 p. m.",
            status = "Rechazado",
            statusColor = RejectedColor,
            icon = Icons.Outlined.DirectionsCar
        )
    )

     */

    val visibleReports = if (selectedFilter == "Todos") {
        reports
    } else {
        reports.filter {
            reportStatusLabel(it.status) == selectedFilter
        }
    }

    Scaffold(
        containerColor = ReportsBackground,
        bottomBar = {
            UrbanBottomBar(
                selectedItem = "Alertas",
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
                top = 20.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ReportsHeader()
            }

            item {
                ReportsSummary(
                    reports = reports,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos",
                        "Pendiente",
                        "Validado",
                        "Rechazado"
                    ).forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                            },
                            modifier = Modifier.weight(1f),
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ReportsPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Historial de reportes",
                    color = ReportsText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ReportsPrimary)
                        }
                    }
                }

                errorMessage.isNotBlank() -> {
                    item {
                        Text(
                            text = errorMessage,
                            color = RejectedColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                visibleReports.isEmpty() -> {
                    item {
                        EmptyReports()
                    }
                }

                else -> {
                    items(visibleReports) { report ->
                        UserReportCard(
                            report = UserReport(
                                title = report.type,
                                location = report.locationName,
                                date = formatReportDate(report.createdAt),
                                status = reportStatusLabel(report.status),
                                statusColor = reportStatusColor(report.status),
                                icon = reportIcon(report.type)
                            ),
                            onClick = {
                                onOpenReport(report)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Mis reportes",
                color = ReportsText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Consulta el estado de tus alertas.",
                color = ReportsText.copy(alpha = 0.63f),
                fontSize = 13.sp
            )
        }

    }
}

@Composable
private fun ReportsSummary(
    reports: List<UrbanReport>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val totalCount = reports.size
    val pendingCount = reports.count { it.status == "pending" }
    val approvedCount = reports.count { it.status == "approved" }
    val rejectedCount = reports.count { it.status == "rejected" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            number = totalCount.toString(),
            label = "Total",
            color = ReportsPrimary,
            selected = selectedFilter == "Todos",
            onClick = { onFilterSelected("Todos") },
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            number = pendingCount.toString(),
            label = "Pendiente",
            color = PendingColor,
            selected = selectedFilter == "Pendiente",
            onClick = { onFilterSelected("Pendiente") },
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            number = approvedCount.toString(),
            label = "Validado",
            color = ApprovedColor,
            selected = selectedFilter == "Validado",
            onClick = { onFilterSelected("Validado") },
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            number = rejectedCount.toString(),
            label = "Rechazado",
            color = RejectedColor,
            selected = selectedFilter == "Rechazado",
            onClick = { onFilterSelected("Rechazado") },
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
private fun SummaryCard(
    number: String,
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (selected) 0.18f else 0.11f)
        ),
        border = if (selected) BorderStroke(1.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = ReportsText.copy(alpha = 0.7f),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}
@Composable
private fun UserReportCard(
    report: UserReport,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(15.dp),
                color = report.statusColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = report.icon,
                        contentDescription = null,
                        tint = report.statusColor,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = report.title,
                        modifier = Modifier.weight(1f),
                        color = ReportsText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = report.statusColor.copy(alpha = 0.13f)
                    ) {
                        Text(
                            text = report.status,
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            ),
                            color = report.statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                ReportInformation(
                    icon = Icons.Outlined.LocationOn,
                    text = report.location
                )

                ReportInformation(
                    icon = Icons.Outlined.CalendarToday,
                    text = report.date
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Ver reporte",
                tint = ReportsText.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun ReportInformation(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ReportsPrimary,
            modifier = Modifier.size(15.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = ReportsText.copy(alpha = 0.63f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun EmptyReports() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = ReportsText.copy(alpha = 0.35f),
            modifier = Modifier.size(55.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No hay reportes en esta categoría",
            color = ReportsText.copy(alpha = 0.6f)
        )
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

private fun reportStatusColor(status: String): Color {
    return when (status) {
        "pending" -> PendingColor
        "approved" -> ApprovedColor
        "rejected" -> RejectedColor
        else -> PendingColor
    }
}

private fun reportIcon(type: String): ImageVector {
    return when (type) {
        "Bache" -> Icons.Outlined.Construction
        "Luminaria" -> Icons.Outlined.Lightbulb
        "Residuos" -> Icons.Outlined.Delete
        "Tránsito" -> Icons.Outlined.DirectionsCar
        "Calle bloqueada" -> Icons.Outlined.Traffic
        else -> Icons.Outlined.ReportProblem
    }
}

private fun formatReportDate(createdAt: Long): String {
    val elapsedMillis = System.currentTimeMillis() - createdAt

    return when {
        elapsedMillis < TimeUnit.MINUTES.toMillis(1) -> "Hace un momento"
        elapsedMillis < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
            "Hace $minutes min"
        }
        elapsedMillis < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
            if (hours == 1L) "Hace 1 hora" else "Hace $hours horas"
        }
        elapsedMillis < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)
            if (days == 1L) "Hace 1 día" else "Hace $days días"
        }
        else -> "Hace más de una semana"
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyReportsPreview() {
    AlertasUrbanasTheme {
        MyReportsScreen()
    }
}





