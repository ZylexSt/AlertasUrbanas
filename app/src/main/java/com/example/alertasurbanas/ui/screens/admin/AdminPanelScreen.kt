package com.example.alertasurbanas.ui.screens.admin

import com.example.alertasurbanas.ui.theme.UrbanColors

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

private val AdminBackground = UrbanColors.Background
private val AdminPrimary = UrbanColors.Primary
private val AdminText = UrbanColors.TextPrimary
private val AdminWarning = UrbanColors.MediumUrgency
private val AdminDanger = UrbanColors.HighUrgency
private val AdminSuccess = UrbanColors.Success

private data class AdminReport(
    val title: String,
    val location: String,
    val reporter: String,
    val time: String,
    val urgency: String,
    val urgencyColor: Color,
    val icon: ImageVector
)

@Composable
fun AdminPanelScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf("Pendientes")
    }

    val reports = listOf(
        AdminReport(
            title = "Bache peligroso",
            location = "Av. Siempre Viva 742",
            reporter = "Juan M.",
            time = "Hace 20 min",
            urgency = "Alta",
            urgencyColor = AdminDanger,
            icon = Icons.Outlined.Construction
        ),
        AdminReport(
            title = "Árbol caído",
            location = "Calle 45 Sur 210",
            reporter = "Laura G.",
            time = "Hace 35 min",
            urgency = "Media",
            urgencyColor = AdminWarning,
            icon = Icons.Outlined.Park
        ),
        AdminReport(
            title = "Luminaria apagada",
            location = "Av. Reforma 1200",
            reporter = "Pedro R.",
            time = "Hace 1 h",
            urgency = "Media",
            urgencyColor = AdminWarning,
            icon = Icons.Outlined.Lightbulb
        )
    )

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
                AdminSummary()
            }

            item {
                Text(
                    text = "Gestión de reportes",
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
                        text = "${reports.size} reportes",
                        color = AdminText.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            items(reports) { report ->
                AdminReportCard(report)
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
                text = "Validación y gestión de incidentes.",
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
private fun AdminSummary() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        AdminSummaryCard(
            number = "12",
            label = "Pendientes",
            color = AdminWarning,
            modifier = Modifier.weight(1f)
        )

        AdminSummaryCard(
            number = "48",
            label = "Validados",
            color = AdminSuccess,
            modifier = Modifier.weight(1f)
        )

        AdminSummaryCard(
            number = "5",
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
    report: AdminReport
) {
    Card(
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
                    color = report.urgencyColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = report.icon,
                            contentDescription = null,
                            tint = report.urgencyColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = report.title,
                            modifier = Modifier.weight(1f),
                            color = AdminText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = report.urgencyColor
                        ) {
                            Text(
                                text = report.urgency,
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
                        text = report.location
                    )

                    AdminInformation(
                        icon = Icons.Outlined.Person,
                        text = "Por ${report.reporter}"
                    )

                    AdminInformation(
                        icon = Icons.Outlined.Schedule,
                        text = report.time
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AdminText.copy(alpha = 0.08f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminSuccess
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text("Validar")
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text("Editar")
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = AdminDanger
                    )
                }
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
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminPanelPreview() {
    AlertasUrbanasTheme {
        AdminPanelScreen()
    }
}






