package com.example.alertasurbanas.car

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.screens.shared.MapTilerMap
import com.example.alertasurbanas.ui.theme.UrbanColors
import org.maplibre.android.geometry.LatLng

@Composable
fun AutomotiveMapDashboardScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UrbanColors.Background)
    ) {
        MapTilerMap(
            modifier = Modifier.fillMaxSize(),
            center = LatLng(31.761, -106.485),
            zoom = 13.2
        )

        AutomotiveTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        AlertPreviewCard(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 94.dp)
                .width(370.dp)
        )

        NearbyAlertsPanel(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 42.dp, end = 28.dp, bottom = 88.dp)
                .width(410.dp)
                .fillMaxHeight()
        )

        MapControls(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 460.dp)
        )

        AutomotiveBottomBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun AutomotiveTopBar(
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
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = "Mapa de alertas",
            color = UrbanColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        AutomotiveChip(
            text = "Tránsito",
            icon = Icons.Outlined.DirectionsCar,
            selected = true
        )

        Spacer(modifier = Modifier.width(14.dp))

        AutomotiveChip(
            text = "Vía pública",
            icon = Icons.Outlined.Construction,
            selected = false
        )

        Spacer(modifier = Modifier.width(14.dp))

        AutomotiveRiskChip()

        Spacer(modifier = Modifier.width(26.dp))

        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = null,
            tint = UrbanColors.TextPrimary,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun AutomotiveChip(
    text: String,
    icon: ImageVector,
    selected: Boolean
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) UrbanColors.Primary else Color.White.copy(alpha = 0.94f))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else UrbanColors.TextPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = if (selected) Color.White else UrbanColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AutomotiveRiskChip() {
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(UrbanColors.HighUrgency.copy(alpha = 0.12f))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = UrbanColors.HighUrgency,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Riesgo alto",
            color = UrbanColors.HighUrgency,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = UrbanColors.TextPrimary
        )
    }
}

@Composable
private fun NearbyAlertsPanel(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Alertas cercanas",
                    color = UrbanColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = UrbanColors.TextPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = UrbanColors.Border)
            Spacer(modifier = Modifier.height(12.dp))

            AutomotiveAlertRow(
                icon = Icons.Outlined.DirectionsCar,
                title = "Choque en Av. Reforma",
                address = "Av. Reforma 1200",
                distance = "250 m",
                level = "Alta",
                color = UrbanColors.HighUrgency
            )

            Divider(color = UrbanColors.Border)

            AutomotiveAlertRow(
                icon = Icons.Outlined.Construction,
                title = "Calle bloqueada",
                address = "Eje Central Lázaro Cárdenas",
                distance = "600 m",
                level = "Media",
                color = UrbanColors.MediumUrgency
            )

            Divider(color = UrbanColors.Border)

            AutomotiveAlertRow(
                icon = Icons.Outlined.LocalFireDepartment,
                title = "Incendio reportado",
                address = "Calz. de Tlalpan 550",
                distance = "1.2 km",
                level = "Alta",
                color = UrbanColors.HighUrgency
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver todas las alertas",
                    color = UrbanColors.Primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = UrbanColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun AutomotiveAlertRow(
    icon: ImageVector,
    title: String,
    address: String,
    distance: String,
    level: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp),
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
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = UrbanColors.TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = address,
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
                    text = level,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = distance,
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(UrbanColors.HighUrgency.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Construction,
                        contentDescription = null,
                        tint = UrbanColors.HighUrgency,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = "Bache peligroso",
                        color = UrbanColors.TextPrimary,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
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
                            text = "Av. Siempre Viva 742",
                            color = UrbanColors.TextSecondary,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(UrbanColors.HighUrgency)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Alta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UrbanColors.Primary)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Ruta segura",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MapControls(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionButton(
            onClick = {},
            containerColor = Color.White,
            contentColor = UrbanColors.TextPrimary
        ) {
            Icon(Icons.Outlined.MyLocation, contentDescription = null)
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Column {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.ZoomIn, contentDescription = null)
                }

                Divider(color = UrbanColors.Border)

                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.ZoomOut, contentDescription = null)
                }
            }
        }
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
        Text(text = "Maps", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(30.dp))
        Text(text = "Teléfono", color = Color.White.copy(alpha = 0.75f), fontSize = 18.sp)
        Spacer(modifier = Modifier.width(30.dp))
        Text(text = "Música", color = Color.White.copy(alpha = 0.75f), fontSize = 18.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "9:41", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(24.dp))
        Text(text = "28°C", color = Color.White, fontSize = 18.sp)
    }
}
