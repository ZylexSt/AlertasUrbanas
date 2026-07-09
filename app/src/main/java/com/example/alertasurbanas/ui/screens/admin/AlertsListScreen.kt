package com.example.alertasurbanas.ui.screens.admin

import com.example.alertasurbanas.ui.theme.UrbanColors


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
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
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

private val AlertsBackground = UrbanColors.Background
private val AlertsPrimary = UrbanColors.Primary
private val AlertsTextPrimary = UrbanColors.TextPrimary
private val AlertsTextSecondary = UrbanColors.TextSecondary
private val AlertsHigh = UrbanColors.HighUrgency
private val AlertsMedium = UrbanColors.MediumUrgency
private val AlertsLow = UrbanColors.LowUrgency

private data class PublicAlert(
    val title: String,
    val category: String,
    val location: String,
    val distance: String,
    val time: String,
    val urgency: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AlertsListScreen(
    onBack: () -> Unit = {},
    onOpenDetail: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    var search by remember { mutableStateOf("") }

    val alerts = listOf(
        PublicAlert(
            title = "Bache peligroso",
            category = "Vía pública",
            location = "Av. Siempre Viva 742",
            distance = "A 120 m",
            time = "Hace 15 min",
            urgency = "Alta",
            icon = Icons.Outlined.Construction,
            color = AlertsHigh
        ),
        PublicAlert(
            title = "Calle bloqueada",
            category = "Tránsito",
            location = "Av. Reforma 1200",
            distance = "A 280 m",
            time = "Hace 25 min",
            urgency = "Alta",
            icon = Icons.Outlined.WarningAmber,
            color = AlertsHigh
        ),
        PublicAlert(
            title = "Luminaria apagada",
            category = "Iluminación",
            location = "Calle 26 Norte 115",
            distance = "A 350 m",
            time = "Hace 1 h",
            urgency = "Media",
            icon = Icons.Outlined.Lightbulb,
            color = AlertsMedium
        ),
        PublicAlert(
            title = "Vehículo obstruyendo",
            category = "Tránsito",
            location = "Blvd. Independencia 250",
            distance = "A 600 m",
            time = "Hace 2 h",
            urgency = "Baja",
            icon = Icons.Outlined.DirectionsCar,
            color = AlertsLow
        ),
        PublicAlert(
            title = "Zona con poca vigilancia",
            category = "Seguridad",
            location = "Parque Central",
            distance = "A 900 m",
            time = "Hoy, 8:20 a. m.",
            urgency = "Media",
            icon = Icons.Outlined.Shield,
            color = AlertsMedium
        )
    )

    val filteredAlerts = alerts.filter { alert ->
        val matchesFilter = selectedFilter == "Todas" || alert.urgency == selectedFilter
        val matchesSearch =
            search.isBlank() ||
                    alert.title.contains(search, ignoreCase = true) ||
                    alert.location.contains(search, ignoreCase = true) ||
                    alert.category.contains(search, ignoreCase = true)

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

            items(filteredAlerts) { alert ->
                PublicAlertCard(
                    alert = alert,
                    onClick = onOpenDetail
                )
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
    alert: PublicAlert,
    onClick: () -> Unit
) {
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
                        color = alert.color.copy(alpha = 0.13f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = alert.icon,
                    contentDescription = null,
                    tint = alert.color,
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
                        text = alert.title,
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
                                color = alert.color.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = alert.urgency,
                            color = alert.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = alert.category,
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
                        text = alert.location,
                        color = AlertsTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "${alert.distance} · ${alert.time}",
                    color = AlertsTextSecondary,
                    fontSize = 13.sp
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







