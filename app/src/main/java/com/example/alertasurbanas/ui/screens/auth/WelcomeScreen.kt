package com.example.alertasurbanas.ui.screens.auth

import com.example.alertasurbanas.ui.theme.UrbanColors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.ui.screens.shared.UrbanAppLogo
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val Background = UrbanColors.Background
private val Primary = UrbanColors.Primary
private val TextPrimary = UrbanColors.TextPrimary
private val TextSecondary = UrbanColors.TextSecondary

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit = {},
    onRegister: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            UrbanAppLogo(size = 104.dp)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "GeoNav",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Reporta incidentes, consulta alertas cercanas y encuentra rutas más seguras.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WelcomeFeature(
                        icon = Icons.Outlined.LocationOn,
                        title = "Alertas cercanas",
                        description = "Consulta incidentes reportados alrededor de tu zona."
                    )
                    WelcomeFeature(
                        icon = Icons.Outlined.Route,
                        title = "Rutas más seguras",
                        description = "Evita zonas con reportes activos durante tu recorrido."
                    )
                    WelcomeFeature(
                        icon = Icons.Outlined.Verified,
                        title = "Información confiable",
                        description = "Los reportes pueden ser revisados por administración."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.4.dp,
                    color = Primary
                )
            ) {
                Text(
                    text = "Crear cuenta",
                    color = Primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WelcomeFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Primary.copy(alpha = 0.10f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.padding(11.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    AlertasUrbanasTheme {
        WelcomeScreen()
    }
}



