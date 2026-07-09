package com.example.alertasurbanas.ui.screens.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.alertasurbanas.ui.theme.UrbanColors

@Composable
fun AdminBottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == "Panel",
            onClick = { onItemSelected("Administrador") },
            icon = { Icon(Icons.Outlined.Dashboard, contentDescription = null) },
            label = { Text("Panel") },
            colors = adminNavigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Reportes",
            onClick = { onItemSelected("ListaAlertas") },
            icon = { Icon(Icons.Outlined.Assignment, contentDescription = null) },
            label = { Text("Reportes") },
            colors = adminNavigationColors()
        )

        NavigationBarItem(
            selected = selectedItem == "Perfil",
            onClick = { onItemSelected("Perfil") },
            icon = { Icon(Icons.Outlined.PersonOutline, contentDescription = null) },
            label = { Text("Perfil") },
            colors = adminNavigationColors()
        )
    }
}

@Composable
private fun adminNavigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = UrbanColors.Primary,
    selectedTextColor = UrbanColors.Primary,
    indicatorColor = UrbanColors.Primary.copy(alpha = 0.12f),
    unselectedIconColor = UrbanColors.TextPrimary.copy(alpha = 0.65f),
    unselectedTextColor = UrbanColors.TextPrimary.copy(alpha = 0.65f)
)


