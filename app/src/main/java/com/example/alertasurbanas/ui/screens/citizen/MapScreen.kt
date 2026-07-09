package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val MapBackground = UrbanColors.MapBackground
private val MapStreet = Color.White
private val MapPrimary = UrbanColors.Primary
private val MapText = UrbanColors.TextPrimary
private val MapHigh = UrbanColors.HighUrgency
private val MapMedium = UrbanColors.MediumUrgency

@Composable
fun MapScreen( onNavigate: (String) -> Unit = {}) {
    Scaffold(
        containerColor = MapBackground,
        bottomBar = {
            UrbanBottomBar(selectedItem = "Mapa",
                onItemSelected = onNavigate)
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FakeCityMap()

            MapHeader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            MapControl(
                icon = Icons.Outlined.MyLocation,
                description = "Mi ubicación",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 84.dp, end = 16.dp)
            )

            MapMarker(
                color = MapMedium,
                icon = Icons.Outlined.Lightbulb,
                modifier = Modifier.offset(
                    x = maxWidth * 0.14f,
                    y = maxHeight * 0.27f
                )
            )

            MapMarker(
                color = MapHigh,
                icon = Icons.Outlined.WarningAmber,
                modifier = Modifier.offset(
                    x = maxWidth * 0.57f,
                    y = maxHeight * 0.20f
                )
            )

            MapMarker(
                color = MapHigh,
                icon = Icons.Outlined.Construction,
                modifier = Modifier.offset(
                    x = maxWidth * 0.62f,
                    y = maxHeight * 0.47f
                )
            )

            MapMarker(
                color = MapPrimary,
                icon = Icons.Outlined.Shield,
                modifier = Modifier.offset(
                    x = maxWidth * 0.77f,
                    y = maxHeight * 0.31f
                )
            )

            CurrentLocation(
                modifier = Modifier.offset(
                    x = maxWidth * 0.30f,
                    y = maxHeight * 0.43f
                )
            )
            SelectedAlertCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onClick = {
                    onNavigate("Detalle")
                }
            )

        }
    }
}

@Composable
private fun FakeCityMap() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(MapBackground)
    ) {
        val minorStreet = 7.dp.toPx()
        val majorStreet = 12.dp.toPx()

        listOf(0.12f, 0.27f, 0.43f, 0.61f, 0.78f).forEach { position ->
            drawLine(
                color = MapStreet,
                start = Offset(0f, size.height * position),
                end = Offset(size.width, size.height * position),
                strokeWidth = minorStreet,
                cap = StrokeCap.Round
            )
        }

        listOf(0.13f, 0.34f, 0.55f, 0.76f, 0.91f).forEach { position ->
            drawLine(
                color = MapStreet,
                start = Offset(size.width * position, 0f),
                end = Offset(size.width * position, size.height),
                strokeWidth = minorStreet,
                cap = StrokeCap.Round
            )
        }

        drawLine(
            color = MapStreet,
            start = Offset(-40f, size.height * 0.75f),
            end = Offset(size.width + 60f, size.height * 0.15f),
            strokeWidth = majorStreet,
            cap = StrokeCap.Round
        )

        drawLine(
            color = MapStreet,
            start = Offset(size.width * 0.18f, 0f),
            end = Offset(size.width * 0.82f, size.height),
            strokeWidth = majorStreet,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = UrbanColors.MapGreenAreaStrong,
            radius = 58.dp.toPx(),
            center = Offset(size.width * 0.78f, size.height * 0.70f)
        )

        drawCircle(
            color = MapStreet,
            radius = 34.dp.toPx(),
            center = Offset(size.width * 0.78f, size.height * 0.70f)
        )
    }
}

@Composable
private fun MapHeader(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MapText.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Buscar una calle o zona",
                modifier = Modifier.weight(1f),
                color = MapText.copy(alpha = 0.6f),
                fontSize = 14.sp
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MapPrimary
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 9.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "Filtros",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MapControl(
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 5.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MapText,
            modifier = Modifier.padding(13.dp)
        )
    }
}

@Composable
private fun MapMarker(
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            shadowElevation = 4.dp
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Alerta",
                tint = Color.White,
                modifier = Modifier
                    .padding(11.dp)
                    .size(22.dp)
            )
        }

        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .size(10.dp)
                .rotate(45f)
                .background(color)
        )
    }
}

@Composable
private fun CurrentLocation(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(25.dp)
            .background(
                color = Color.White,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .background(
                    color = UrbanColors.CurrentLocation,
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun SelectedAlertCard(    modifier: Modifier = Modifier,
                                  onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 7.dp
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .padding(top = 9.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .background(
                        color = UrbanColors.MapLine,
                        shape = CircleShape
                    )
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .background(
                            color = MapHigh.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Construction,
                        contentDescription = null,
                        tint = MapHigh,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bache peligroso",
                            modifier = Modifier.weight(1f),
                            color = MapText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = MapHigh
                        ) {
                            Text(
                                text = "Alta",
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

                    MapInformation(
                        icon = Icons.Outlined.LocationOn,
                        text = "Av. Siempre Viva 742"
                    )

                    MapInformation(
                        icon = Icons.Outlined.NearMe,
                        text = "A 120 m"
                    )

                    MapInformation(
                        icon = Icons.Outlined.Schedule,
                        text = "Hace 15 min"
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver alerta",
                    tint = MapText
                )
            }
        }
    }
}

@Composable
private fun MapInformation(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MapPrimary,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = MapText.copy(alpha = 0.68f),
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MapScreenPreview() {
    AlertasUrbanasTheme {
        MapScreen()
    }
}




