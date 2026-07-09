package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val DetailBackground = UrbanColors.Background
private val DetailPrimary = UrbanColors.Primary
private val DetailText = UrbanColors.TextPrimary
private val DetailDanger = UrbanColors.HighUrgency

@Composable
fun DetailAlertScreen(
    onBack: () -> Unit = {},
    onSafeRoute: () -> Unit = {}
) {
    Scaffold(
        containerColor = DetailBackground,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onSafeRoute,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DetailPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ver ruta segura",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                DetailHeader(onBack = onBack)
            }

            item {
                EvidenceImage()
            }

            item {
                AlertTitle()
            }

            item {
                AlertMetadata()
            }

            item {
                HorizontalDivider(
                    color = DetailText.copy(alpha = 0.1f)
                )
            }

            item {
                DescriptionSection()
            }

            item {
                RecommendationCard()
            }
        }
    }
}

@Composable
private fun DetailHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = DetailText
            )
        }

        Text(
            text = "Detalle de alerta",
            modifier = Modifier.weight(1f),
            color = DetailText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "Guardar",
                tint = DetailText
            )
        }

        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Compartir",
                tint = DetailText
            )
        }
    }
}

@Composable
private fun EvidenceImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(22.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(UrbanColors.EvidenceDark)

            val cracks = listOf(
                Offset(0.05f, 0.18f) to Offset(0.42f, 0.45f),
                Offset(0.92f, 0.12f) to Offset(0.60f, 0.44f),
                Offset(0.10f, 0.85f) to Offset(0.43f, 0.60f),
                Offset(0.93f, 0.82f) to Offset(0.59f, 0.58f),
                Offset(0.30f, 0.05f) to Offset(0.48f, 0.40f)
            )

            cracks.forEach { crack ->
                drawLine(
                    color = UrbanColors.EvidenceLine,
                    start = Offset(
                        size.width * crack.first.x,
                        size.height * crack.first.y
                    ),
                    end = Offset(
                        size.width * crack.second.x,
                        size.height * crack.second.y
                    ),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            drawOval(
                color = UrbanColors.EvidenceStrong,
                topLeft = Offset(
                    size.width * 0.24f,
                    size.height * 0.30f
                ),
                size = Size(
                    size.width * 0.52f,
                    size.height * 0.45f
                )
            )

            drawOval(
                color = UrbanColors.EvidenceMuted,
                topLeft = Offset(
                    size.width * 0.31f,
                    size.height * 0.38f
                ),
                size = Size(
                    size.width * 0.38f,
                    size.height * 0.28f
                )
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.62f)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 7.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Evidencia del reporte",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AlertTitle() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bache peligroso",
                color = DetailText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Vía pública",
                color = DetailPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            shape = RoundedCornerShape(9.dp),
            color = DetailDanger
        ) {
            Text(
                text = "Urgencia alta",
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 7.dp
                ),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AlertMetadata() {
    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        MetadataRow(
            icon = Icons.Outlined.LocationOn,
            text = "Av. Siempre Viva 742, Col. Centro"
        )

        MetadataRow(
            icon = Icons.Outlined.Schedule,
            text = "Reportado hace 15 minutos"
        )

        MetadataRow(
            icon = Icons.Outlined.Visibility,
            text = "23 personas han consultado esta alerta"
        )
    }
}

@Composable
private fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DetailPrimary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = text,
            color = DetailText.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DescriptionSection() {
    Column {
        Text(
            text = "Descripción",
            color = DetailText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bache profundo que representa riesgo para vehículos y motocicletas, especialmente durante la noche o cuando la calle se encuentra mojada.",
            color = DetailText.copy(alpha = 0.72f),
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun RecommendationCard() {
    Card(
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = DetailPrimary.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = DetailPrimary
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Recomendación",
                    color = DetailPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = "Utiliza una ruta alternativa para evitar esta zona.",
                    color = DetailText.copy(alpha = 0.72f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetailAlertPreview() {
    AlertasUrbanasTheme {
        DetailAlertScreen()
    }
}




