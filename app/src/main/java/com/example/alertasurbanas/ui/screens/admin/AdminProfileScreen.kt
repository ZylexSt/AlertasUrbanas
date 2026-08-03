package com.example.alertasurbanas.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.UserProfile
import com.example.alertasurbanas.ui.theme.UrbanColors

private data class AdminProfileOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun AdminProfileScreen(
    profile: UserProfile = UserProfile(
        name = "Administrador",
        email = "admin@email.com",
        role = "admin"
    ),
    isSaving: Boolean = false,
    message: String = "",
    onNavigate: (String) -> Unit = {},
    onUpdateProfile: (name: String, email: String, currentPassword: String) -> Unit = { _, _, _ -> },
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {}
) {
    var showEditProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = UrbanColors.Background,
        bottomBar = {
            AdminBottomBar(
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
                start = 22.dp,
                end = 22.dp,
                top = 28.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = "Perfil administrador",
                    color = UrbanColors.TextPrimary,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            item {
                AdminProfileHeaderCard(
                    profile = profile,
                    onEdit = { showEditProfileDialog = true }
                )
            }

            if (message.isNotBlank()) {
                item {
                    Text(
                        text = message,
                        color = if (message.contains("correct", ignoreCase = true)) UrbanColors.Primary else UrbanColors.HighUrgency,
                        fontSize = 13.sp
                    )
                }
            }

            item {
                SectionTitle("Administracion")
            }

            item {
                AdminProfileCard {
                    AdminProfileItem(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "Validacion de reportes",
                        subtitle = "Permisos para aprobar o rechazar alertas"
                    )

                    HorizontalDivider(color = UrbanColors.Border)

                    AdminProfileItem(
                        icon = Icons.Outlined.Tune,
                        title = "Preferencias del panel",
                        subtitle = "Filtros, orden y visualizacion"
                    )

                    HorizontalDivider(color = UrbanColors.Border)

                    AdminProfileItem(
                        icon = Icons.Outlined.Notifications,
                        title = "Notificaciones",
                        subtitle = "Avisos de reportes pendientes"
                    )
                }
            }

            item {
                SectionTitle("Seguridad")
            }

            item {
                AdminProfileCard {
                    AdminProfileMenuItem(
                        option = AdminProfileOption(
                            title = "Privacidad y seguridad",
                            subtitle = "Editar datos y cambiar contrasena",
                            icon = Icons.Outlined.Lock
                        ),
                        onClick = { showPasswordDialog = true }
                    )

                    HorizontalDivider(color = UrbanColors.Border)

                    AdminProfileItem(
                        icon = Icons.Outlined.Security,
                        title = "Actividad de cuenta",
                        subtitle = "Ultimos accesos y acciones realizadas"
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, UrbanColors.HighUrgency.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = UrbanColors.HighUrgency
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Cerrar sesion",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showEditProfileDialog) {
        AdminEditProfileDialog(
            profile = profile,
            isSaving = isSaving,
            onDismiss = { showEditProfileDialog = false },
            onSave = onUpdateProfile
        )
    }

    if (showPasswordDialog) {
        AdminChangePasswordDialog(
            isSaving = isSaving,
            onDismiss = { showPasswordDialog = false },
            onSave = onChangePassword
        )
    }
}

@Composable
private fun AdminProfileHeaderCard(
    profile: UserProfile,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(UrbanColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.initials,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name.ifBlank { "Administrador" },
                    color = UrbanColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = profile.email.ifBlank { "Sin correo" },
                    color = UrbanColors.TextSecondary,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = UrbanColors.SoftGreen
                ) {
                    Text(
                        text = profile.roleLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = UrbanColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar perfil",
                    tint = UrbanColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = UrbanColors.TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun AdminProfileCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
private fun AdminProfileItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminMenuIcon(icon)

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = UrbanColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                color = UrbanColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AdminProfileMenuItem(
    option: AdminProfileOption,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminMenuIcon(option.icon)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                color = UrbanColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = option.subtitle,
                color = UrbanColors.TextSecondary,
                fontSize = 13.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = UrbanColors.TextSecondary
        )
    }
}

@Composable
private fun AdminMenuIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(UrbanColors.SoftGreen),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UrbanColors.Primary,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun AdminEditProfileDialog(
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
                    AdminPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contrasena actual",
                        visible = showPassword,
                        onToggleVisible = { showPassword = !showPassword }
                    )

                    Text(
                        text = "Para cambiar el correo necesitamos confirmar tu contrasena.",
                        color = UrbanColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "Rol: ${profile.roleLabel}",
                    color = UrbanColors.Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, email, password) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = UrbanColors.Primary)
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
private fun AdminChangePasswordDialog(
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
        title = { Text("Cambiar contrasena", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminPasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Contrasena actual",
                    visible = showCurrentPassword,
                    onToggleVisible = { showCurrentPassword = !showCurrentPassword }
                )

                AdminPasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nueva contrasena",
                    visible = showNewPassword,
                    onToggleVisible = { showNewPassword = !showNewPassword }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(currentPassword, newPassword) },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = UrbanColors.Primary)
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
private fun AdminPasswordField(
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