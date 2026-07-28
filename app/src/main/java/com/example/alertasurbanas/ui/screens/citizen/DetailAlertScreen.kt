package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.R
import com.example.alertasurbanas.model.UrbanReport
import com.example.alertasurbanas.ui.theme.UrbanColors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import java.util.concurrent.TimeUnit

private val DetailBackground = UrbanColors.Background
private val DetailPrimary = UrbanColors.Primary
private val DetailText = UrbanColors.TextPrimary
private val DetailDanger = UrbanColors.HighUrgency

@Composable
fun DetailAlertScreen(
    report: UrbanReport? = null,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    canReview: Boolean = false,
    isDeleting: Boolean = false,
    isReviewing: Boolean = false,
    onBack: () -> Unit = {},
    onSafeRoute: () -> Unit = {},
    onEditReport: () -> Unit = {},
    onDeleteReport: () -> Unit = {},
    onApproveReport: () -> Unit = {},
    onRejectReport: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val activeReport = report ?: UrbanReport(
        type = "Reporte",
        description = "Selecciona un reporte para ver su detalle.",
        urgency = "Media",
        locationName = "Sin ubicación"
    )
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
                DetailHeader(
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onBack = onBack,
                    onEditClick = onEditReport,
                    onDeleteClick = {
                        showDeleteDialog = true
                    }
                )
            }

            item {
                EvidenceImage(reportType = activeReport.type)
            }

            item {
                AlertTitle(report = activeReport)
            }

            item {
                AlertMetadata(report = activeReport)
            }

            item {
                HorizontalDivider(
                    color = DetailText.copy(alpha = 0.1f)
                )
            }

            item {
                DescriptionSection(description = activeReport.description)
            }

            item {
                RecommendationCard()
            }

            if (canReview) {
                item {
                    AdminReviewActions(
                        isReviewing = isReviewing,
                        onApproveReport = onApproveReport,
                        onRejectReport = onRejectReport
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) showDeleteDialog = false
            },
            title = {
                Text(text = "Eliminar reporte")
            },
            text = {
                Text(text = "Esta acción eliminará el reporte de forma permanente.")
            },
            confirmButton = {
                TextButton(
                    onClick = onDeleteReport,
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Eliminar", color = DetailDanger)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                    enabled = !isDeleting
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DetailHeader(
    canEdit: Boolean,
    canDelete: Boolean,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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

        if (canEdit) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar reporte",
                    tint = DetailText
                )
            }
        }

        if (canDelete) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar reporte",
                    tint = DetailDanger
                )
            }
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
private fun EvidenceImage(reportType: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(22.dp))
    ) {
        Image(
            painter = painterResource(id = reportImageForType(reportType)),
            contentDescription = "Evidencia predeterminada del reporte",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                    text = "Imagen de referencia",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
@Composable
private fun AlertTitle(report: UrbanReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = report.type.ifBlank { "Reporte urbano" },
                color = DetailText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = reportStatusLabel(report.status),
                color = DetailPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            shape = RoundedCornerShape(9.dp),
            color = urgencyColor(report.urgency)
        ) {
            Text(
                text = "Urgencia ${report.urgency.lowercase()}",
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
private fun AlertMetadata(report: UrbanReport) {
    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        MetadataRow(
            icon = Icons.Outlined.LocationOn,
            text = report.locationName.ifBlank { "Sin ubicación" }
        )

        MetadataRow(
            icon = Icons.Outlined.Schedule,
            text = formatReportDate(report.createdAt)
        )

        MetadataRow(
            icon = Icons.Outlined.Person,
            text = report.userName.ifBlank { "Usuario ciudadano" }
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
private fun DescriptionSection(description: String) {
    Column {
        Text(
            text = "Descripción",
            color = DetailText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description.ifBlank { "Este reporte no tiene descripción." },
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

@Composable
private fun AdminReviewActions(
    isReviewing: Boolean,
    onApproveReport: () -> Unit,
    onRejectReport: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Revision de administrador",
                color = DetailText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRejectReport,
                    enabled = !isReviewing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DetailDanger
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text("Rechazar")
                }

                Button(
                    onClick = onApproveReport,
                    enabled = !isReviewing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UrbanColors.Success
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text("Validar")
                }
            }
        }
    }
}

private fun formatReportDate(createdAt: Long): String {
    val elapsedMillis = System.currentTimeMillis() - createdAt

    return when {
        elapsedMillis < TimeUnit.MINUTES.toMillis(1) -> "Reportado hace un momento"
        elapsedMillis < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
            "Reportado hace $minutes min"
        }
        elapsedMillis < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
            if (hours == 1L) "Reportado hace 1 hora" else "Reportado hace $hours horas"
        }
        elapsedMillis < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)
            if (days == 1L) "Reportado hace 1 día" else "Reportado hace $days días"
        }
        else -> "Reportado hace más de una semana"
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

private fun urgencyColor(urgency: String): Color {
    return when (urgency) {
        "Alta" -> UrbanColors.HighUrgency
        "Media" -> UrbanColors.MediumUrgency
        "Baja" -> UrbanColors.Primary
        else -> DetailDanger
    }
}

private fun reportImageForType(type: String): Int {
    val normalizedType = type.lowercase()

    return when {
        normalizedType.contains("bache") -> R.drawable.report_bache
        normalizedType.contains("luminaria") -> R.drawable.report_luminaria
        normalizedType.contains("iluminacion") -> R.drawable.report_luminaria
        normalizedType.contains("iluminación") -> R.drawable.report_luminaria
        normalizedType.contains("residuo") -> R.drawable.report_residuos
        normalizedType.contains("transito") -> R.drawable.report_transito
        normalizedType.contains("tránsito") -> R.drawable.report_transito
        else -> R.drawable.report_bache
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetailAlertPreview() {
    AlertasUrbanasTheme {
        DetailAlertScreen()
    }
}







