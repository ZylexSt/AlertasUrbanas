package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import androidx.compose.foundation.lazy.LazyColumn


private val ReportBackground = UrbanColors.Background
private val ReportPrimary = UrbanColors.Primary
private val ReportText = UrbanColors.TextPrimary
private val ReportMedium = UrbanColors.MediumUrgency
private val ReportHigh = UrbanColors.HighUrgency

private data class ReportType(
    val name: String,
    val icon: ImageVector
)

@Composable
fun CreateReportScreen(
    onNavigate: (String) -> Unit = {},
    onSelectLocation: () -> Unit = {},
    selectedType: String = "Bache",
    onTypeSelected: (String) -> Unit = {},
    selectedUrgency: String = "Media",
    onUrgencySelected: (String) -> Unit = {},
    description: String = "",
    onDescriptionChange: (String) -> Unit = {},
    locationName: String = "Av. Independencia 250, Col. Centro",
    screenTitle: String = "Crear reporte",
    screenSubtitle: String = "Ayúdanos a mantener informada a la comunidad.",
    submitButtonText: String = "Enviar reporte",
    isLoading: Boolean = false,
    errorMessage: String = "",
    onSubmitReport: (
        type: String,
        description: String,
        urgency: String,
        locationName: String
    ) -> Unit = { _, _, _, _ -> }
) {

    var localError by rememberSaveable {
        mutableStateOf("")
    }

    val reportTypes = listOf(
        ReportType("Bache", Icons.Outlined.Construction),
        ReportType("Luminaria", Icons.Outlined.Lightbulb),
        ReportType("Residuos", Icons.Outlined.Delete),
        ReportType("Tránsito", Icons.Outlined.DirectionsCar),
        ReportType("Calle bloqueada", Icons.Outlined.Traffic),
        ReportType("Otro", Icons.Outlined.MoreHoriz)
    )

    Scaffold(
        containerColor = ReportBackground,
        bottomBar = {
            UrbanBottomBar(
                selectedItem = "Reportar",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ReportHeader(
                title = screenTitle,
                subtitle = screenSubtitle
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    SectionTitle("¿Qué quieres reportar?")
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reportTypes) { type ->
                            ReportTypeCard(
                                type = type,
                                selected = selectedType == type.name,
                                onClick = {
                                    onTypeSelected(type.name)
                                }
                            )
                        }
                    }
                }

                item {
                    SectionTitle("Ubicación")
                }

                item {
                    LocationSelector(
                        onClick = onSelectLocation
                    )
                }

                item {
                    SectionTitle("Descripción")
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= 200) {
                                onDescriptionChange(it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp),
                        placeholder = {
                            Text("Cuéntanos más sobre la situación...")
                        },
                        supportingText = {
                            Text("${description.length}/200")
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ReportPrimary,
                            unfocusedBorderColor = UrbanColors.BorderMuted
                        )
                    )
                }

                item {
                    SectionTitle("Nivel de urgencia")
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UrgencyOption(
                            title = "Baja",
                            color = ReportPrimary,
                            selected = selectedUrgency == "Baja",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUrgencySelected("Baja")
                            }
                        )

                        UrgencyOption(
                            title = "Media",
                            color = ReportMedium,
                            selected = selectedUrgency == "Media",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUrgencySelected("Media")
                            }
                        )

                        UrgencyOption(
                            title = "Alta",
                            color = ReportHigh,
                            selected = selectedUrgency == "Alta",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUrgencySelected("Alta")
                            }
                        )
                    }
                }

                item {
                    SectionTitle("Imagen de referencia")
                }

                item {
                    DefaultImageNotice(reportType = selectedType)
                }

                item {
                    val visibleError = localError.ifBlank { errorMessage }

                    if (visibleError.isNotBlank()) {
                        Text(
                            text = visibleError,
                            color = ReportHigh,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            localError = when {
                                description.trim().isBlank() -> "Agrega una descripcion del reporte."
                                else -> ""
                            }

                            if (localError.isBlank()) {
                                onSubmitReport(
                                    selectedType,
                                    description.trim(),
                                    selectedUrgency,
                                    locationName
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ReportPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            Text(
                                text = submitButtonText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 12.dp
        )
    ) {
        Text(
            text = title,
            color = ReportText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            color = ReportText.copy(alpha = 0.65f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = ReportText,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ReportTypeCard(
    type: ReportType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(88.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                ReportPrimary
            } else {
                Color.White
            }
        ),
        border = if (selected) {
            null
        } else {
            BorderStroke(1.dp, UrbanColors.Border)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                tint = if (selected) Color.White else ReportText
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = type.name,
                color = if (selected) Color.White else ReportText,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LocationSelector(onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, UrbanColors.BorderMuted)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ReportPrimary.copy(alpha = 0.10f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = ReportPrimary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ubicación del incidente",
                    color = ReportText,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Selecciona un punto en el mapa",
                    color = ReportText.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = ReportText
            )
        }
    }
}

@Composable
private fun UrgencyOption(
    title: String,
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = if (selected) {
            color.copy(alpha = 0.13f)
        } else {
            Color.White
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) color else UrbanColors.BorderMuted
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 13.dp
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.PriorityHigh,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(17.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = title,
                color = if (selected) color else ReportText,
                fontSize = 12.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}

@Composable
private fun DefaultImageNotice(reportType: String) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            ReportText.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = ReportPrimary.copy(alpha = 0.10f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = ReportPrimary,
                    modifier = Modifier.padding(11.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Imagen asignada automaticamente",
                    color = ReportText,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Se usara una imagen de $reportType en el detalle.",
                    color = ReportText.copy(alpha = 0.62f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CreateReportPreview() {
    AlertasUrbanasTheme {
        CreateReportScreen()
    }
}







