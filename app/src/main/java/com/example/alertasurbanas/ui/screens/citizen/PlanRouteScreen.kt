package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

private val RouteBackground = UrbanColors.Background
private val RoutePrimary = UrbanColors.Primary
private val RouteAccent = UrbanColors.Terracotta
private val RouteText = UrbanColors.TextPrimary
private val RouteDanger = UrbanColors.HighUrgency

@Composable
fun PlanRouteScreen(
    onBack: () -> Unit = {},
    onStartRoute: () -> Unit = {}
) {
    var selectedOption by rememberSaveable {
        mutableStateOf("Más segura")
    }

    Scaffold(
        containerColor = RouteBackground,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onStartRoute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoutePrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Iniciar recorrido",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RouteHeader(onBack = onBack)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
            ) {
                RouteMap()

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(15.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 5.dp
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = RouteText,
                        modifier = Modifier.padding(13.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.52f),
                shape = RoundedCornerShape(
                    topStart = 25.dp,
                    topEnd = 25.dp
                ),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .background(
                                UrbanColors.MapLine,
                                CircleShape
                            )
                            .align(Alignment.CenterHorizontally)
                    )

                    RouteLocationField(
                        icon = Icons.Outlined.MyLocation,
                        label = "Origen",
                        value = "Mi ubicación actual",
                        color = UrbanColors.CurrentLocation
                    )

                    RouteLocationField(
                        icon = Icons.Outlined.LocationOn,
                        label = "Destino",
                        value = "Parque Central",
                        color = RouteAccent
                    )

                    Text(
                        text = "Preferencia de ruta",
                        color = RouteText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Más rápida", "Más segura").forEach {
                            FilterChip(
                                selected = selectedOption == it,
                                onClick = {
                                    selectedOption = it
                                },
                                label = {
                                    Text(it)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (it == "Más segura") {
                                            Icons.Outlined.Shield
                                        } else {
                                            Icons.Outlined.Speed
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoutePrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }

                    RouteSummary()
                }
            }
        }
    }
}

@Composable
private fun RouteHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouteBackground)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = RouteText
            )
        }

        Column {
            Text(
                text = "Planear ruta",
                color = RouteText,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Encuentra el recorrido más seguro.",
                color = RouteText.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RouteMap() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(UrbanColors.NeutralPanel)
    ) {
        val streetWidth = 8.dp.toPx()

        listOf(0.18f, 0.40f, 0.64f, 0.84f).forEach {
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height * it),
                end = Offset(size.width, size.height * it),
                strokeWidth = streetWidth
            )
        }

        listOf(0.18f, 0.40f, 0.62f, 0.84f).forEach {
            drawLine(
                color = Color.White,
                start = Offset(size.width * it, 0f),
                end = Offset(size.width * it, size.height),
                strokeWidth = streetWidth
            )
        }

        drawLine(
            color = Color.White,
            start = Offset(-30f, size.height * 0.88f),
            end = Offset(size.width, size.height * 0.16f),
            strokeWidth = 14.dp.toPx()
        )

        val route = Path().apply {
            moveTo(
                size.width * 0.15f,
                size.height * 0.78f
            )
            lineTo(
                size.width * 0.28f,
                size.height * 0.64f
            )
            lineTo(
                size.width * 0.43f,
                size.height * 0.64f
            )
            lineTo(
                size.width * 0.43f,
                size.height * 0.42f
            )
            lineTo(
                size.width * 0.66f,
                size.height * 0.42f
            )
            lineTo(
                size.width * 0.79f,
                size.height * 0.22f
            )
        }

        drawPath(
            path = route,
            color = Color.White,
            style = Stroke(
                width = 14.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        drawPath(
            path = route,
            color = RoutePrimary,
            style = Stroke(
                width = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = Offset(
                size.width * 0.15f,
                size.height * 0.78f
            )
        )

        drawCircle(
            color = UrbanColors.CurrentLocation,
            radius = 8.dp.toPx(),
            center = Offset(
                size.width * 0.15f,
                size.height * 0.78f
            )
        )

        drawCircle(
            color = RouteAccent,
            radius = 11.dp.toPx(),
            center = Offset(
                size.width * 0.79f,
                size.height * 0.22f
            )
        )

        drawCircle(
            color = RouteDanger,
            radius = 10.dp.toPx(),
            center = Offset(
                size.width * 0.54f,
                size.height * 0.42f
            )
        )
    }
}

@Composable
private fun RouteLocationField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    OutlinedCard(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )

            Spacer(modifier = Modifier.width(11.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = RouteText.copy(alpha = 0.58f),
                    fontSize = 11.sp
                )

                Text(
                    text = value,
                    color = RouteText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Editar",
                tint = RouteText.copy(alpha = 0.5f),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun RouteSummary() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RoutePrimary.copy(alpha = 0.09f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem(
                value = "12 min",
                label = "Tiempo"
            )

            SummaryItem(
                value = "4.2 km",
                label = "Distancia"
            )

            SummaryItem(
                value = "2",
                label = "Alertas",
                valueColor = RouteDanger
            )
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    valueColor: Color = RoutePrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = RouteText.copy(alpha = 0.62f),
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PlanRoutePreview() {
    AlertasUrbanasTheme {
        PlanRouteScreen()
    }
}




