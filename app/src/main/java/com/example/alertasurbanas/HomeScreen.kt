package com.example.alertasurbanas.ui.screens

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
import com.example.alertasurbanas.ui.screens.AIRecommendationsScreen


private val Background = Color(0xFFF6F4F0)
private val Primary = Color(0xFF3F6862)
private val TextPrimary = Color(0xFF202A2E)
private val Terracotta = Color(0xFFC8754A)
private val HighUrgency = Color(0xFFD9534F)
private val MediumUrgency = Color(0xFFE7A33E)

private data class AlertCategory(
    val name: String,
    val icon: ImageVector
)

private data class UrbanAlert(
    val title: String,
    val location: String,
    val distance: String,
    val time: String,
    val urgency: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HomeScreen( onNavigate: (String) -> Unit = {}) {
    val categories = listOf(
        AlertCategory("Todas", Icons.Outlined.GridView),
        AlertCategory("Tránsito", Icons.Outlined.DirectionsCar),
        AlertCategory("Vía pública", Icons.Outlined.Construction),
        AlertCategory("Iluminación", Icons.Outlined.Lightbulb),
        AlertCategory("Seguridad", Icons.Outlined.Shield)
    )

    val alerts = listOf(
        UrbanAlert(
            title = "Bache peligroso",
            location = "Av. Siempre Viva 742",
            distance = "A 120 m",
            time = "Hace 15 min",
            urgency = "Alta",
            icon = Icons.Outlined.Construction,
            color = HighUrgency
        ),
        UrbanAlert(
            title = "Luminaria apagada",
            location = "Calle 26 Norte 115",
            distance = "A 350 m",
            time = "Hace 1 h",
            urgency = "Media",
            icon = Icons.Outlined.Lightbulb,
            color = MediumUrgency
        )
    )

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
            item { Header() }
            item { SearchBar() }
            item { NearbyAlert() }

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

                    Text(
                        text = "Ver todas",
                        color = Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            items(alerts) { alert ->
                AlertCard(alert)
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "¡Hola, Ana!",
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
            unfocusedBorderColor = Color(0xFFE4E1DC)
        )
    )
}

@Composable
private fun NearbyAlert() {
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
                    text = "Bache peligroso a 120 m de ti",
                    fontSize = 12.sp,
                    color = TextPrimary.copy(alpha = 0.65f)
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
private fun AlertCard(alert: UrbanAlert) {
    Card(
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
                        color = alert.color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = alert.icon,
                    contentDescription = null,
                    tint = alert.color,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.title,
                        modifier = Modifier.weight(1f),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = alert.color
                    ) {
                        Text(
                            text = alert.urgency,
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
                    text = alert.location
                )

                AlertInformation(
                    icon = Icons.Outlined.NearMe,
                    text = alert.distance
                )

                AlertInformation(
                    icon = Icons.Outlined.Schedule,
                    text = alert.time
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