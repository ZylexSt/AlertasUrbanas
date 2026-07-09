package com.example.alertasurbanas.ui.screens.citizen

import com.example.alertasurbanas.ui.theme.UrbanColors

import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onNavigate: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var notificationsEnabled by rememberSaveable {
        mutableStateOf(true)
    }

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
                UserProfileCard()
            }

            item {
                ProfileStatistics()
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
                        )
                    )
                }
            }

            item {
                SectionLabel("Preferencias")
            }

            item {
                ProfileMenuCard {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MenuIcon(Icons.Outlined.Notifications)

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificaciones",
                                color = ProfileText,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Alertas cercanas y actualizaciones",
                                color = ProfileText.copy(alpha = 0.58f),
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ProfilePrimary
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 62.dp),
                        color = ProfileText.copy(alpha = 0.08f)
                    )

                    ProfileMenuOption(
                        option = ProfileOption(
                            title = "Apariencia",
                            subtitle = "Tema y visualización",
                            icon = Icons.Outlined.Palette
                        )
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
}

@Composable
private fun UserProfileCard() {
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
                        text = "AM",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(15.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ana Martínez",
                    color = ProfileText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "ana@email.com",
                    color = ProfileText.copy(alpha = 0.62f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(7.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ProfilePrimary.copy(alpha = 0.11f)
                ) {
                    Text(
                        text = "Ciudadana",
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

            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Editar",
                tint = ProfileText.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun ProfileStatistics() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileStatistic(
            value = "3",
            label = "Reportes",
            modifier = Modifier.weight(1f)
        )

        ProfileStatistic(
            value = "1",
            label = "Validado",
            modifier = Modifier.weight(1f)
        )

        ProfileStatistic(
            value = "5",
            label = "Guardados",
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
    option: ProfileOption
) {
    Row(
        modifier = Modifier.padding(14.dp),
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

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ProfileText.copy(alpha = 0.5f)
        )
    }
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






