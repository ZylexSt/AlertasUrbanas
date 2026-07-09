package com.example.alertasurbanas.ui.screens.auth

import com.example.alertasurbanas.ui.theme.UrbanColors

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = UrbanColors.Background
private val Primary = UrbanColors.Primary
private val TextPrimary = UrbanColors.TextPrimary
private val TextSecondary = UrbanColors.TextSecondary
private val Terracotta = UrbanColors.Terracotta
private val ErrorRed = UrbanColors.HighUrgency

@Composable
fun LoginScreen(
    errorMessage: String = "",
    isLoading: Boolean = false,
    onLogin: (String, String) -> Unit = { _, _ -> },
    onRegister: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    val visibleError = localError.ifBlank { errorMessage }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 26.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bienvenido",
            color = TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Inicia sesión para consultar y reportar alertas urbanas.",
            color = TextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        AuthTabs(
            selected = "login",
            onLogin = {},
            onRegister = onRegister
        )

        Spacer(modifier = Modifier.height(28.dp))

        AuthField(
            label = "Correo electrónico",
            value = email,
            onValueChange = {
                email = it
                localError = ""
            },
            leadingIcon = Icons.Outlined.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                color = ErrorRed,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPassword) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val cleanEmail = email.trim()

                localError = when {
                    cleanEmail.isBlank() -> "Ingresa tu correo electrónico."
                    !cleanEmail.contains("@") || !cleanEmail.contains(".") -> "Ingresa un correo válido."
                    password.isBlank() -> "Ingresa tu contraseña."
                    password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
                    else -> ""
                }

                if (localError.isBlank()) {
                    onLogin(cleanEmail, password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "Iniciar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "O inicia con",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialButton("G", Modifier.weight(1f))
            SocialButton("f", Modifier.weight(1f))
            SocialButton("●", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun AuthTabs(
    selected: String,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(UrbanColors.NeutralChip)
            .padding(4.dp)
    ) {
        AuthTabButton(
            text = "Log In",
            selected = selected == "login",
            onClick = onLogin,
            modifier = Modifier.weight(1f)
        )

        AuthTabButton(
            text = "Sign Up",
            selected = selected == "register",
            onClick = onRegister,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AuthTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.White else Color.Transparent,
            contentColor = if (selected) TextPrimary else TextSecondary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 1.dp else 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Primary
                )
            },
            trailingIcon = trailingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = UrbanColors.Border,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun SocialButton(
    text: String,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, UrbanColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Text(
            text = text,
            color = Terracotta,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}




