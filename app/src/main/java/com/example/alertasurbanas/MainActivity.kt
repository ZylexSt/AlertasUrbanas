package com.example.alertasurbanas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.alertasurbanas.ui.screens.HomeScreen
import com.example.alertasurbanas.ui.screens.MapScreen
import com.example.alertasurbanas.ui.screens.DetailAlertScreen
import com.example.alertasurbanas.ui.screens.CreateReportScreen
import com.example.alertasurbanas.ui.screens.SelectLocationScreen
import com.example.alertasurbanas.ui.screens.MyReportsScreen
import com.example.alertasurbanas.ui.screens.PlanRouteScreen
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import com.example.alertasurbanas.ui.screens.AIRecommendationsScreen
import com.example.alertasurbanas.ui.screens.ProfileScreen
import com.example.alertasurbanas.ui.screens.AdminPanelScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlertasUrbanasTheme {
                var selectedScreen by rememberSaveable {
                    mutableStateOf("Inicio")
                }

                when (selectedScreen) {
                    "Mapa" -> MapScreen(
                        onNavigate = { selectedScreen = it }
                    )
                    "Detalle" -> DetailAlertScreen(
                        onBack = {
                            selectedScreen = "Mapa"
                        },
                        onSafeRoute = {
                            selectedScreen = "PlanearRuta"
                        }
                    )
                    "Reportar" -> CreateReportScreen(
                        onNavigate = { selectedScreen = it },
                        onSelectLocation = {
                            selectedScreen = "SeleccionarUbicacion"
                        }
                    )
                    "SeleccionarUbicacion" -> SelectLocationScreen(
                        onBack = {
                            selectedScreen = "Reportar"
                        },
                        onConfirm = {
                            selectedScreen = "Reportar"
                        }
                    )
                    "Alertas" -> MyReportsScreen(
                        onNavigate = {
                            selectedScreen = it
                        }
                    )

                    "PlanearRuta" -> PlanRouteScreen(
                        onBack = {
                            selectedScreen = "Detalle"
                        },
                        onStartRoute = {
                            selectedScreen = "Mapa"
                        }
                    )
                    "IA" -> AIRecommendationsScreen(
                        onBack = {
                            selectedScreen = "Inicio"
                        },
                        onNavigate = {
                            selectedScreen = it
                        }
                    )
                    "Perfil" -> ProfileScreen(
                        onNavigate = {
                            selectedScreen = it
                        },
                        onOpenAdmin = {
                            selectedScreen = "Administrador"
                        }
                    )
                    "Administrador" -> AdminPanelScreen(
                        onBack = {
                            selectedScreen = "Perfil"
                        }
                    )

                    else -> HomeScreen(
                        onNavigate = { selectedScreen = it }
                    )
                }
            }
        }
    }
}