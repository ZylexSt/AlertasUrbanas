package com.example.alertasurbanas.ui.screens.auth

import com.example.alertasurbanas.ui.theme.UrbanColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RegisterBackground = UrbanColors.Background
private val RegisterPrimary = UrbanColors.Primary
private val RegisterTextPrimary = UrbanColors.TextPrimary
private val RegisterTextSecondary = UrbanColors.TextSecondary
private val RegisterErrorRed = UrbanColors.HighUrgency

@Composable
fun RegisterScreen(
    errorMessage: String = "",
    isLoading: Boolean = false,
    onRegister: (String, String, String) -> Unit = { _, _, _ -> },
    onLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    val visibleError = localError.ifBlank { errorMessage }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterBackground)
            .padding(horizontal = 26.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(RegisterPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Crear cuenta",
            color = RegisterTextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Regístrate para reportar incidentes y consultar alertas cercanas.",
            color = RegisterTextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthTabs(
            selected = "register",
            onLogin = onLogin,
            onRegister = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AuthField(
                label = "Nombre",
                value = name,
                onValueChange = {
                    name = it
                    localError = ""
                },
                leadingIcon = Icons.Outlined.Person,
                modifier = Modifier.weight(1f)
            )

            AuthField(
                label = "Apellido",
                value = lastName,
                onValueChange = {
                    lastName = it
                    localError = ""
                },
                leadingIcon = Icons.Outlined.Person,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AuthField(
            label = "Correo electrónico",
            value = email,
            onValueChange = {
                email = it
                localError = ""
            },
            leadingIcon = Icons.Outlined.Email
        )

        Spacer(modifier = Modifier.height(14.dp))

        AuthField(
            label = "Contraseña",
            value = password,
            onValueChange = {
                password = it
                localError = ""
            },
            leadingIcon = Icons.Outlined.Lock,
            trailingIcon = Icons.Outlined.VisibilityOff,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (visibleError.isNotBlank()) {
            Text(
                text = visibleError,
                color = RegisterErrorRed,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val cleanName = name.trim()
                val cleanLastName = lastName.trim()
                val cleanEmail = email.trim()

                localError = when {
                    cleanName.isBlank() -> "Ingresa tu nombre."
                    cleanLastName.isBlank() -> "Ingresa tu apellido."
                    cleanEmail.isBlank() -> "Ingresa tu correo electrónico."
                    !cleanEmail.contains("@") || !cleanEmail.contains(".") -> "Ingresa un correo válido."
                    password.isBlank() -> "Crea una contraseña."
                    password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                    else -> ""
                }

                if (localError.isBlank()) {
                    onRegister("$cleanName $cleanLastName", cleanEmail, password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RegisterPrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Registrarme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(onClick = onLogin) {
            Text(
                text = "Ya tengo cuenta",
                color = RegisterPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}




