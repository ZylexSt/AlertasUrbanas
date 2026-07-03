package com.example.alertasurbanas.ui.screens

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

private val ReportBackground = Color(0xFFF6F4F0)
private val ReportPrimary = Color(0xFF3F6862)
private val ReportText = Color(0xFF202A2E)
private val ReportMedium = Color(0xFFE7A33E)
private val ReportHigh = Color(0xFFD9534F)

private data class ReportType(
    val name: String,
    val icon: ImageVector
)

@Composable
fun CreateReportScreen(
    onNavigate: (String) -> Unit = {},
    onSelectLocation: () -> Unit = {}

) {
    var selectedType by rememberSaveable {
        mutableStateOf("Bache")
    }

    var selectedUrgency by rememberSaveable {
        mutableStateOf("Media")
    }

    var description by rememberSaveable {
        mutableStateOf("")
    }

    val reportTypes = listOf(
        ReportType("Bache", Icons.Outlined.Construction),
        ReportType("Luminaria", Icons.Outlined.Lightbulb),
        ReportType("Residuos", Icons.Outlined.Delete),
        ReportType("Tránsito", Icons.Outlined.DirectionsCar),
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
            ReportHeader()

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
                                    selectedType = type.name
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
                                description = it
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
                            unfocusedBorderColor = Color(0xFFD9D6D1)
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
                                selectedUrgency = "Baja"
                            }
                        )

                        UrgencyOption(
                            title = "Media",
                            color = ReportMedium,
                            selected = selectedUrgency == "Media",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedUrgency = "Media"
                            }
                        )

                        UrgencyOption(
                            title = "Alta",
                            color = ReportHigh,
                            selected = selectedUrgency == "Alta",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedUrgency = "Alta"
                            }
                        )
                    }
                }

                item {
                    SectionTitle("Foto de evidencia (opcional)")
                }

                item {
                    PhotoSelector()
                }

                item {
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ReportPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Text(
                            text = "Enviar reporte",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportHeader() {
    Column(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 12.dp
        )
    ) {
        Text(
            text = "Crear reporte",
            color = ReportText,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Ayúdanos a mantener informada a la comunidad.",
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
            BorderStroke(1.dp, Color(0xFFE0DDD8))
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
        border = BorderStroke(1.dp, Color(0xFFD9D6D1))
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
            color = if (selected) color else Color(0xFFD9D6D1)
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
private fun PhotoSelector() {
    OutlinedCard(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            1.dp,
            ReportText.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = null,
                tint = ReportPrimary,
                modifier = Modifier.size(30.dp)
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Agregar fotografía",
                color = ReportText,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Cámara o galería",
                color = ReportText.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
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