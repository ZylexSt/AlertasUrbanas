package com.example.alertasurbanas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.alertasurbanas.ui.screens.citizen.AIRecommendationsScreen
import com.example.alertasurbanas.ui.screens.admin.AdminPanelScreen
import com.example.alertasurbanas.ui.screens.admin.AlertsListScreen
import com.example.alertasurbanas.ui.screens.citizen.CreateReportScreen
import com.example.alertasurbanas.ui.screens.citizen.DetailAlertScreen
import com.example.alertasurbanas.ui.screens.citizen.HomeScreen
import com.example.alertasurbanas.ui.screens.auth.LoginScreen
import com.example.alertasurbanas.ui.screens.citizen.MapScreen
import com.example.alertasurbanas.ui.screens.citizen.MyReportsScreen
import com.example.alertasurbanas.ui.screens.citizen.PlanRouteScreen
import com.example.alertasurbanas.ui.screens.citizen.ProfileScreen
import com.example.alertasurbanas.ui.screens.auth.RegisterScreen
import com.example.alertasurbanas.ui.screens.citizen.SelectLocationScreen
import com.example.alertasurbanas.ui.screens.auth.WelcomeScreen
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import kotlinx.coroutines.launch
import com.example.alertasurbanas.ui.screens.admin.AdminProfileScreen
import com.example.alertasurbanas.data.ReportRepository
import com.example.alertasurbanas.model.UrbanReport
import androidx.compose.runtime.LaunchedEffect



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlertasUrbanasTheme {
                var selectedScreen by rememberSaveable {
                    mutableStateOf("Bienvenida")
                }

                val authManager = remember { AuthManager() }
                val scope = rememberCoroutineScope()

                val reportRepository = remember { ReportRepository() }
                var reportErrorMessage by rememberSaveable { mutableStateOf("") }
                var isReportLoading by rememberSaveable { mutableStateOf(false) }
                var reportType by rememberSaveable { mutableStateOf("Bache") }
                var reportUrgency by rememberSaveable { mutableStateOf("Media") }
                var reportDescription by rememberSaveable { mutableStateOf("") }
                var reportLocationName by rememberSaveable {
                    mutableStateOf("Av. Independencia 250, Col. Centro")
                }

                var myReports by remember { mutableStateOf<List<UrbanReport>>(emptyList()) }
                var selectedReport by remember { mutableStateOf<UrbanReport?>(null) }
                var myReportsError by rememberSaveable { mutableStateOf("") }
                var isMyReportsLoading by rememberSaveable { mutableStateOf(false) }
                var isDeleteReportLoading by rememberSaveable { mutableStateOf(false) }

                var errorMessage by rememberSaveable { mutableStateOf("") }
                var isAuthLoading by rememberSaveable { mutableStateOf(false) }
                var currentUserRole by rememberSaveable { mutableStateOf("citizen") }

                when (selectedScreen) {
                    "Bienvenida" -> WelcomeScreen(
                        onLogin = {
                            errorMessage = ""
                            selectedScreen = "Login"
                        },
                        onRegister = {
                            errorMessage = ""
                            selectedScreen = "Registro"
                        }
                    )

                    "Login" -> LoginScreen(
                        errorMessage = errorMessage,
                        isLoading = isAuthLoading,
                        onLogin = { email, password ->
                            scope.launch {
                                try {
                                    errorMessage = ""
                                    isAuthLoading = true

                                    val role = authManager.login(email, password)
                                    currentUserRole = role

                                    selectedScreen = if (role == "admin") {
                                        "Administrador"
                                    } else {
                                        "Inicio"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "No se pudo iniciar sesiÃ³n"
                                } finally {
                                    isAuthLoading = false
                                }
                            }
                        },
                        onRegister = {
                            errorMessage = ""
                            selectedScreen = "Registro"
                        },
                        onForgotPassword = {
                            // Pendiente: recuperaciÃ³n de contraseÃ±a
                        }
                    )

                    "Registro" -> RegisterScreen(
                        errorMessage = errorMessage,
                        isLoading = isAuthLoading,
                        onRegister = { name, email, password ->
                            scope.launch {
                                try {
                                    errorMessage = ""
                                    isAuthLoading = true

                                    authManager.registerCitizen(
                                        name = name,
                                        email = email,
                                        password = password
                                    )

                                    selectedScreen = "Inicio"
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "No se pudo registrar el usuario"
                                } finally {
                                    isAuthLoading = false
                                }
                            }
                        },
                        onLogin = {
                            errorMessage = ""
                            selectedScreen = "Login"
                        }
                    )

                    "Mapa" -> MapScreen(
                        onNavigate = { selectedScreen = it }
                    )

                    "Detalle" -> DetailAlertScreen(
                        report = selectedReport,
                        canDelete = selectedReport?.id?.isNotBlank() == true,
                        isDeleting = isDeleteReportLoading,
                        onBack = {
                            selectedScreen = "Alertas"
                        },
                        onSafeRoute = {
                            selectedScreen = "PlanearRuta"
                        },
                        onDeleteReport = {
                            val reportToDelete = selectedReport

                            if (reportToDelete?.id?.isNotBlank() == true) {
                                scope.launch {
                                    try {
                                        isDeleteReportLoading = true
                                        myReportsError = ""

                                        reportRepository.deleteReport(reportToDelete.id)
                                        myReports = myReports.filterNot { it.id == reportToDelete.id }
                                        selectedReport = null
                                        selectedScreen = "Alertas"
                                    } catch (e: Exception) {
                                        myReportsError = e.message ?: "No se pudo eliminar el reporte."
                                    } finally {
                                        isDeleteReportLoading = false
                                    }
                                }
                            }
                        }
                    )

                    "Reportar" -> CreateReportScreen(
                        onNavigate = { selectedScreen = it },
                        onSelectLocation = {
                            selectedScreen = "SeleccionarUbicacion"
                        },
                        selectedType = reportType,
                        onTypeSelected = {
                            reportType = it
                        },
                        selectedUrgency = reportUrgency,
                        onUrgencySelected = {
                            reportUrgency = it
                        },
                        description = reportDescription,
                        onDescriptionChange = {
                            reportDescription = it
                        },
                        locationName = reportLocationName,
                        isLoading = isReportLoading,
                        errorMessage = reportErrorMessage,
                        onSubmitReport = { type, description, urgency, locationName ->
                            scope.launch {
                                try {
                                    reportErrorMessage = ""
                                    isReportLoading = true

                                    reportRepository.createReport(
                                        UrbanReport(
                                            type = type,
                                            description = description,
                                            urgency = urgency,
                                            locationName = locationName
                                        )
                                    )

                                    reportType = "Bache"
                                    reportUrgency = "Media"
                                    reportDescription = ""
                                    reportLocationName = "Av. Independencia 250, Col. Centro"


                                    selectedScreen = "Alertas"
                                } catch (e: Exception) {
                                    reportErrorMessage = e.message ?: "No se pudo crear el reporte."
                                } finally {
                                    isReportLoading = false
                                }

                            }
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

                    "Alertas" -> {
                        LaunchedEffect(Unit) {
                            try {
                                myReportsError = ""
                                isMyReportsLoading = true
                                myReports = reportRepository.getMyReports()
                            } catch (e: Exception) {
                                myReportsError = e.message ?: "No se pudieron cargar tus reportes."
                            } finally {
                                isMyReportsLoading = false
                            }
                        }

                        MyReportsScreen(
                            reports = myReports,
                            isLoading = isMyReportsLoading,
                            errorMessage = myReportsError,
                            onOpenReport = { report ->
                                selectedReport = report
                                selectedScreen = "Detalle"
                            },
                            onNavigate = {
                                selectedScreen = it
                            }
                        )
                    }

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

                    "Perfil" -> {
                        if (currentUserRole == "admin") {
                            AdminProfileScreen(
                                onNavigate = {
                                    selectedScreen = it
                                },
                                onLogout = {
                                    authManager.logout()
                                    currentUserRole = "citizen"
                                    selectedScreen = "Bienvenida"
                                }
                            )
                        } else {
                            ProfileScreen(
                                onNavigate = {
                                    selectedScreen = it
                                },
                                onLogout = {
                                    authManager.logout()
                                    currentUserRole = "citizen"
                                    selectedScreen = "Bienvenida"
                                }
                            )
                        }
                    }

                    "Administrador" -> AdminPanelScreen(
                        onBack = {
                            selectedScreen = "Perfil"
                        },
                        onNavigate = {
                            selectedScreen = it
                        }
                    )

                    "ListaAlertas" -> AlertsListScreen(
                        onBack = {
                            selectedScreen = "Inicio"
                        },
                        onOpenDetail = {
                            selectedScreen = "Detalle"
                        },
                        onNavigate = {
                            selectedScreen = it
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





