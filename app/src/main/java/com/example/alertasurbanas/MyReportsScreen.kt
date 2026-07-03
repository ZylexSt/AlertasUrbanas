package com.example.alertasurbanas.ui.screens

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
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val ReportsBackground = Color(0xFFF6F4F0)
private val ReportsPrimary = Color(0xFF3F6862)
private val ReportsText = Color(0xFF202A2E)
private val PendingColor = Color(0xFFE7A33E)
private val ApprovedColor = Color(0xFF4F8A6D)
private val RejectedColor = Color(0xFFD9534F)

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
    onNavigate: (String) -> Unit = {}
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf("Todos")
    }

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

    val visibleReports = if (selectedFilter == "Todos") {
        reports
    } else {
        reports.filter {
            it.status == selectedFilter
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
                ReportsSummary()
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(
                        listOf(
                            "Todos",
                            "Pendiente",
                            "Validado",
                            "Rechazado"
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

            if (visibleReports.isEmpty()) {
                item {
                    EmptyReports()
                }
            } else {
                items(visibleReports) { report ->
                    UserReportCard(report)
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

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Filtros",
                tint = ReportsText,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun ReportsSummary() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            number = "3",
            label = "Total",
            color = ReportsPrimary,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            number = "1",
            label = "Pendiente",
            color = PendingColor,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            number = "1",
            label = "Validado",
            color = ApprovedColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = ReportsText.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun UserReportCard(report: UserReport) {
    Card(
        onClick = {},
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MyReportsPreview() {
    AlertasUrbanasTheme {
        MyReportsScreen()
    }
}