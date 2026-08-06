package com.example.alertasurbanas.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alertasurbanas.UserProfile
import com.example.alertasurbanas.ui.theme.UrbanColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    users: List<UserProfile> = emptyList(),
    currentUserId: String = "",
    isLoading: Boolean = false,
    errorMessage: String = "",
    updatingUserId: String = "",
    onBack: () -> Unit = {},
    onRoleSelected: (UserProfile, String) -> Unit = { _, _ -> },
    onNavigate: (String) -> Unit = {}
) {
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Regresar",
                            tint = UrbanColors.TextPrimary
                        )
                    }

                    Column {
                        Text(
                            text = "Gestion de usuarios",
                            color = UrbanColors.TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cambia el rol de cada usuario.",
                            color = UrbanColors.TextPrimary.copy(alpha = 0.62f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = UrbanColors.Primary)
                    }
                }
            } else if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = UrbanColors.HighUrgency,
                        fontSize = 13.sp
                    )
                }
            } else if (users.isEmpty()) {
                item {
                    Text(
                        text = "No hay usuarios registrados.",
                        color = UrbanColors.TextPrimary.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
            } else {
                items(users, key = { it.uid }) { user ->
                    AdminUserCard(
                        user = user,
                        isCurrentUser = user.uid == currentUserId,
                        isUpdating = updatingUserId == user.uid,
                        onRoleSelected = { newRole -> onRoleSelected(user, newRole) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: UserProfile,
    isCurrentUser: Boolean,
    isUpdating: Boolean,
    onRoleSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = UrbanColors.Primary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = if (user.role == "admin") Icons.Outlined.AdminPanelSettings else Icons.Outlined.PersonOutline,
                    contentDescription = null,
                    tint = UrbanColors.Primary,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { "Usuario sin nombre" },
                    color = UrbanColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user.email.ifBlank { "Sin correo" },
                    color = UrbanColors.TextPrimary.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isCurrentUser) "${user.roleLabel} - Tu cuenta" else user.roleLabel,
                    color = UrbanColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (user.role == "admin") "Admin" else "Ciudadano",
                    color = if (isCurrentUser) UrbanColors.TextPrimary.copy(alpha = 0.45f) else UrbanColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = UrbanColors.Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Switch(
                        checked = user.role == "admin",
                        onCheckedChange = { checked ->
                            onRoleSelected(if (checked) "admin" else "citizen")
                        },
                        enabled = !isCurrentUser,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = UrbanColors.Primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = UrbanColors.TextPrimary.copy(alpha = 0.28f),
                            disabledCheckedThumbColor = Color.White,
                            disabledCheckedTrackColor = UrbanColors.Primary.copy(alpha = 0.35f)
                        )
                    )
                }
            }
        }
    }
}