package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val AIBackground = UrbanColors.Background
private val AIPrimary = UrbanColors.Primary
private val AIText = UrbanColors.TextPrimary
private val AIOrange = UrbanColors.Terracotta
private val AIWarning = UrbanColors.MediumUrgency
private val AIDanger = UrbanColors.HighUrgency

private data class AIRecommendation(
    val title: String,
    val description: String,
    val label: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun AIRecommendationsScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    var selectedPeriod by rememberSaveable {
        mutableStateOf("Ahora")
    }

    val recommendations = listOf(
        AIRecommendation(
            title = "Evita Av. Reforma",
            description = "Se detectaron varios incidentes y tráfico lento entre las 5:00 y 7:00 p. m.",
            label = "Riesgo alto",
            color = AIDanger,
            icon = Icons.Outlined.WarningAmber
        ),
        AIRecommendation(
            title = "Utiliza Blvd. Central",
            description = "Esta alternativa presenta menos alertas y podría ahorrar aproximadamente 8 minutos.",
            label = "Ruta sugerida",
            color = AIPrimary,
            icon = Icons.Outlined.Route
        ),
        AIRecommendation(
            title = "Precaución en zona centro",
            description = "Los reportes de baches aumentan después de lluvias intensas.",
            label = "Predicción",
            color = AIWarning,
            icon = Icons.Outlined.AutoGraph
        )
    )

    Scaffold(
        containerColor = AIBackground,
        bottomBar = {
            UrbanBottomBar(
                selectedItem = "IA",
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
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AIHeader(onBack = onBack)
            }

            item {
                AIHeroCard()
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    listOf("Ahora", "Hoy", "Esta semana").forEach {
                        FilterChip(
                            selected = selectedPeriod == it,
                            onClick = {
                                selectedPeriod = it
                            },
                            label = {
                                Text(it)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AIPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Análisis de tu zona",
                    color = AIText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                RiskMapCard()
            }

            item {
                Text(
                    text = "Recomendaciones para ti",
                    color = AIText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recommendations) {
                AIRecommendationCard(it)
            }

            item {
                AIInformationCard()
            }
        }
    }
}

@Composable
private fun AIHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = AIText
            )
        }

        Column {
            Text(
                text = "Recomendaciones de IA",
                color = AIText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Análisis inteligente de riesgos urbanos.",
                color = AIText.copy(alpha = 0.62f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AIHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        AIPrimary,
                        UrbanColors.PrimaryLight
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(11.dp)
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "Riesgo moderado en tu zona",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "La IA analizó reportes recientes, horarios y zonas con incidentes recurrentes.",
                color = Color.White.copy(alpha = 0.83f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun RiskMapCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(UrbanColors.NeutralPanel)
            ) {
                val street = 7.dp.toPx()

                listOf(0.2f, 0.5f, 0.8f).forEach {
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, size.height * it),
                        end = Offset(size.width, size.height * it),
                        strokeWidth = street
                    )
                }

                listOf(0.2f, 0.5f, 0.8f).forEach {
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * it, 0f),
                        end = Offset(size.width * it, size.height),
                        strokeWidth = street
                    )
                }

                drawCircle(
                    color = AIDanger.copy(alpha = 0.32f),
                    radius = 48.dp.toPx(),
                    center = Offset(
                        size.width * 0.67f,
                        size.height * 0.40f
                    )
                )

                drawCircle(
                    color = AIWarning.copy(alpha = 0.32f),
                    radius = 38.dp.toPx(),
                    center = Offset(
                        size.width * 0.28f,
                        size.height * 0.64f
                    )
                )

                drawCircle(
                    color = AIPrimary.copy(alpha = 0.35f),
                    radius = 22.dp.toPx(),
                    center = Offset(
                        size.width * 0.52f,
                        size.height * 0.73f
                    )
                )
            }

            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = AIPrimary
                )

                Spacer(modifier = Modifier.width(9.dp))

                Column {
                    Text(
                        text = "3 zonas analizadas",
                        color = AIText,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Actualizado hace 5 minutos",
                        color = AIText.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AIRecommendationCard(
    recommendation: AIRecommendation
) {
    Card(
        onClick = {},
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = recommendation.color.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = recommendation.icon,
                    contentDescription = null,
                    tint = recommendation.color,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    color = AIText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = recommendation.description,
                    color = AIText.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = recommendation.label,
                    color = recommendation.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AIText.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun AIInformationCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AIOrange.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AIOrange
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Estas recomendaciones son orientativas y se generan a partir de reportes ciudadanos. Mantén atención a las condiciones reales del camino.",
                color = AIText.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AIRecommendationsPreview() {
    AlertasUrbanasTheme {
        AIRecommendationsScreen()
    }
}




