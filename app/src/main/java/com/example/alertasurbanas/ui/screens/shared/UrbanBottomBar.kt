package com.example.alertasurbanas.ui.screens.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.alertasurbanas.ui.theme.UrbanColors

@Composable
fun UrbanBottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == "Inicio",
            onClick = { onItemSelected("Inicio") },
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
            label = { Text("Inicio") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Mapa",
            onClick = { onItemSelected("Mapa") },
            icon = { Icon(Icons.Outlined.Map, contentDescription = null) },
            label = { Text("Mapa") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Reportar",
            onClick = { onItemSelected("Reportar") },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = UrbanColors.Primary
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(9.dp)
                    )
                }
            },
            label = { Text("Reportar") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Alertas",
            onClick = { onItemSelected("Alertas") },
            icon = { Icon(Icons.Outlined.NotificationsNone, contentDescription = null) },
            label = { Text("Alertas") },
            colors = navigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Perfil",
            onClick = { onItemSelected("Perfil") },
            icon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null) },
            label = { Text("Perfil") },
            colors = navigationColors()
        )
    }
}

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = UrbanColors.Primary,
    selectedTextColor = UrbanColors.Primary,
    indicatorColor = UrbanColors.Primary.copy(alpha = 0.12f),
    unselectedIconColor = UrbanColors.TextPrimary.copy(alpha = 0.65f),
    unselectedTextColor = UrbanColors.TextPrimary.copy(alpha = 0.65f)
)

