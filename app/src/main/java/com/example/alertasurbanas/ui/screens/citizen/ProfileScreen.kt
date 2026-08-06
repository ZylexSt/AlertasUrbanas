package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.UserProfile
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme

private val ProfileBackground = UrbanColors.Background
private val ProfilePrimary = UrbanColors.Primary
private val ProfileText = UrbanColors.TextPrimary
private val ProfileDanger = UrbanColors.HighUrgency

private data class ProfileOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun ProfileScreen(
    profile: UserProfile = UserProfile(
        name = "Usuario",
        email = "usuario@email.com",
        role = "citizen"
    ),
    isSaving: Boolean = false,
    message: String = "",
    unreadNotificationsCount: Int = 0,
    totalReports: Int = 0,
    validatedReports: Int = 0,
    rejectedReports: Int = 0,
    onNavigate: (String) -> Unit = {},
    onUpdateProfile: (name: String, email: String, currentPassword: String) -> Unit = { _, _, _ -> },
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {}
) {
    var showEditProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = ProfileBackground,
        bottomBar = {
            UrbanBottomBar(
                selectedItem = "Perfil",
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
                top = 22.dp,
                bottom = 25.dp
            ),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            item {
                Text(
                    text = "Perfil",
                    color = ProfileText,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                UserProfileCard(
                    profile = profile,
                    onEdit = { showEditProfileDialog = true }
                )
            }

            if (message.isNotBlank()) {
                item {
                    Text(
                        text = message,
                        color = if (message.contains("correct", ignoreCase = true)) ProfilePrimary else ProfileDanger,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                ProfileStatistics(
                    totalReports = totalReports,
                    validatedReports = validatedReports,
                    rejectedReports = rejectedReports
                )
            }

            item {
                SectionLabel("Cuenta")
            }

            item {
                ProfileMenuCard {
                    ProfileMenuOption(
                        option = ProfileOption(
                            title = "Privacidad y seguridad",
                            subtitle = "Contraseña y uso de datos",
                            icon = Icons.Outlined.Security
                        ),
                        onClick = { showPasswordDialog = true }
                    )
                }
            }

            item {
                SectionLabel("Preferencias")
            }

            item {
                ProfileMenuCard {
                    ProfileMenuOption(
                        option = ProfileOption(
                            title = "Notificaciones",
                            subtitle = "Alertas cercanas y actualizaciones",
                            icon = Icons.Outlined.Notifications
                        ),
                        badgeCount = unreadNotificationsCount,
                        onClick = { onNavigate("Notificaciones") }
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(
                        1.dp,
                        ProfileDanger.copy(alpha = 0.65f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ProfileDanger
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Cerrar sesión",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            profile = profile,
            isSaving = isSaving,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, password ->
                onUpdateProfile(name, email, password)
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            isSaving = isSaving,
            onDismiss = { showPasswordDialog = false },
            onSave = { currentPassword, newPassword ->
                onChangePassword(currentPassword, newPassword)
            }
        )
    }
}

@Composable
private fun UserProfileCard(
    profile: UserProfile,
    onEdit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = ProfilePrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profile.initials,
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.ifBlank { "Usuario" },
                    color = ProfileText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = profile.email.ifBlank { "Sin correo" },
                    color = ProfileText.copy(alpha = 0.62f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(7.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ProfilePrimary.copy(alpha = 0.11f)
                ) {
                    Text(
                        text = profile.roleLabel,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),
                        color = ProfilePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    tint = ProfileText.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatistics(
    totalReports: Int,
    validatedReports: Int,
    rejectedReports: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileStatistic(
            value = totalReports.toString(),
            label = "Reportes",
            modifier = Modifier.weight(1f)
        )

        ProfileStatistic(
            value = validatedReports.toString(),
            label = "Validados",
            modifier = Modifier.weight(1f)
        )

        ProfileStatistic(
            value = rejectedReports.toString(),
            label = "Rechazados",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileStatistic(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfilePrimary.copy(alpha = 0.09f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = ProfilePrimary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = ProfileText.copy(alpha = 0.62f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = ProfileText,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileMenuCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        content = content
    )
}

@Composable
private fun ProfileMenuOption(
    option: ProfileOption,
    badgeCount: Int = 0,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuIcon(option.icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                color = ProfileText,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = option.subtitle,
                color = ProfileText.copy(alpha = 0.58f),
                fontSize = 11.sp
            )
        }

        if (badgeCount > 0) {
            NotificationBadge(count = badgeCount)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ProfileText.copy(alpha = 0.5f)
        )
    }
}


@Composable
private fun NotificationBadge(count: Int) {
    Surface(
        shape = CircleShape,
        color = UrbanColors.HighUrgency
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, currentPassword: String) -> Unit
) {
    var name by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var email by rememberSaveable(profile.email) { mutableStateOf(profile.email) }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val emailChanged = !email.trim().equals(profile.email.trim(), ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (emailChanged) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña actual") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Para cambiar el correo necesitamos confirmar tu contraseña.",
                        color = ProfileText.copy(alpha = 0.62f),
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "Rol: ${profile.roleLabel}",
                    color = ProfilePrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, email, password) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = ProfilePrimary)
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (currentPassword: String, newPassword: String) -> Unit
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var showCurrentPassword by rememberSaveable { mutableStateOf(false) }
    var showNewPassword by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Contraseña actual",
                    visible = showCurrentPassword,
                    onToggleVisible = { showCurrentPassword = !showCurrentPassword }
                )

                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nueva contraseña",
                    visible = showNewPassword,
                    onToggleVisible = { showNewPassword = !showNewPassword }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currentPassword, newPassword) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = ProfilePrimary)
            ) {
                Text(if (isSaving) "Actualizando..." else "Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MenuIcon(icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ProfilePrimary.copy(alpha = 0.10f)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfilePrimary,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfilePreview() {
    AlertasUrbanasTheme {
        ProfileScreen()
    }
}






