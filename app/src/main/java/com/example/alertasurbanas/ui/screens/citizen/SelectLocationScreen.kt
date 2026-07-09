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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val LocationBackground = UrbanColors.MapBackground
private val LocationPrimary = UrbanColors.Primary
private val LocationAccent = UrbanColors.Terracotta
private val LocationText = UrbanColors.TextPrimary

@Composable
fun SelectLocationScreen(
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    Scaffold(
        containerColor = LocationBackground,
        bottomBar = {
            LocationConfirmation(
                onConfirm = onConfirm
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LocationMapBackground()

            LocationSearchHeader(
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 85.dp, end = 16.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 5.dp
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "Mi ubicación",
                    tint = LocationText,
                    modifier = Modifier.padding(13.dp)
                )
            }

            CenterLocationMarker(
                modifier = Modifier.align(Alignment.Center)
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 65.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Text(
                    text = "Mueve el mapa para ajustar",
                    modifier = Modifier.padding(
                        horizontal = 13.dp,
                        vertical = 8.dp
                    ),
                    color = LocationText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun LocationMapBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(LocationBackground)
    ) {
        val smallStreet = 8.dp.toPx()
        val mainStreet = 14.dp.toPx()

        listOf(0.13f, 0.29f, 0.47f, 0.65f, 0.83f).forEach {
            drawLine(
                color = Color.White,
                start = Offset(0f, size.height * it),
                end = Offset(size.width, size.height * it),
                strokeWidth = smallStreet,
                cap = StrokeCap.Round
            )
        }

        listOf(0.14f, 0.36f, 0.58f, 0.80f).forEach {
            drawLine(
                color = Color.White,
                start = Offset(size.width * it, 0f),
                end = Offset(size.width * it, size.height),
                strokeWidth = smallStreet,
                cap = StrokeCap.Round
            )
        }

        drawLine(
            color = Color.White,
            start = Offset(-50f, size.height * 0.78f),
            end = Offset(size.width + 50f, size.height * 0.15f),
            strokeWidth = mainStreet,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.22f, 0f),
            end = Offset(size.width * 0.75f, size.height),
            strokeWidth = mainStreet,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = UrbanColors.MapGreenArea,
            radius = 62.dp.toPx(),
            center = Offset(
                size.width * 0.20f,
                size.height * 0.72f
            )
        )

        drawCircle(
            color = Color.White,
            radius = 35.dp.toPx(),
            center = Offset(
                size.width * 0.20f,
                size.height * 0.72f
            )
        )
    }
}

@Composable
private fun LocationSearchHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 5.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = LocationText,
                modifier = Modifier.padding(13.dp)
            )
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(17.dp),
            color = Color.White,
            shadowElevation = 5.dp
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = LocationText.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.width(9.dp))

                Text(
                    text = "Buscar una dirección",
                    color = LocationText.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CenterLocationMarker(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.offset(y = (-25).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = LocationAccent,
            shadowElevation = 7.dp
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Ubicación seleccionada",
                tint = Color.White,
                modifier = Modifier
                    .padding(13.dp)
                    .size(30.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(3.dp)
                .height(17.dp)
                .background(LocationAccent)
        )

        Box(
            modifier = Modifier
                .size(
                    width = 30.dp,
                    height = 7.dp
                )
                .background(
                    color = Color.Black.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun LocationConfirmation(
    onConfirm: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .background(
                        color = UrbanColors.Handle,
                        shape = CircleShape
                    )
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(17.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = LocationPrimary.copy(alpha = 0.10f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = LocationPrimary,
                        modifier = Modifier.padding(11.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ubicación seleccionada",
                        color = LocationText.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    Text(
                        text = "Av. Independencia 250, Col. Centro",
                        color = LocationText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(17.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocationPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Confirmar ubicación",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SelectLocationPreview() {
    AlertasUrbanasTheme {
        SelectLocationScreen()
    }
}





