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
import com.example.alertasurbanas.data.EmailNotificationService
import com.example.alertasurbanas.data.NotificationRepository
import com.example.alertasurbanas.data.ReportRepository
import com.example.alertasurbanas.data.UrbanAlert
import com.example.alertasurbanas.model.AppNotification
import com.example.alertasurbanas.model.UrbanReport
import androidx.compose.runtime.LaunchedEffect
import com.example.alertasurbanas.ui.screens.shared.NotificationsScreen



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
                val notificationRepository = remember { NotificationRepository() }
                var reportErrorMessage by rememberSaveable { mutableStateOf("") }
                var isReportLoading by rememberSaveable { mutableStateOf(false) }
                var reportType by rememberSaveable { mutableStateOf("Bache") }
                var reportUrgency by rememberSaveable { mutableStateOf("Media") }
                var reportDescription by rememberSaveable { mutableStateOf("") }
                var reportLocationName by rememberSaveable {
                    mutableStateOf("Av. Independencia 250, Col. Centro")
                }
                var reportLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
                var reportLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

                var myReports by remember { mutableStateOf<List<UrbanReport>>(emptyList()) }
                var selectedReport by remember { mutableStateOf<UrbanReport?>(null) }
                var myReportsError by rememberSaveable { mutableStateOf("") }
                var isMyReportsLoading by rememberSaveable { mutableStateOf(false) }

                var allReports by remember { mutableStateOf<List<UrbanReport>>(emptyList()) }
                var adminReportsError by rememberSaveable { mutableStateOf("") }
                var isAdminReportsLoading by rememberSaveable { mutableStateOf(false) }
                var isReviewReportLoading by rememberSaveable { mutableStateOf(false) }

                var isDeleteReportLoading by rememberSaveable { mutableStateOf(false) }
                var editingReportId by rememberSaveable { mutableStateOf("") }
                var locationReturnScreen by rememberSaveable { mutableStateOf("Reportar") }
                var publicDetailBackScreen by rememberSaveable { mutableStateOf("Inicio") }
                var notificationsBackScreen by rememberSaveable { mutableStateOf("Inicio") }

                var errorMessage by rememberSaveable { mutableStateOf("") }
                var isAuthLoading by rememberSaveable { mutableStateOf(false) }
                var currentUserRole by rememberSaveable { mutableStateOf("citizen") }
                var currentUserProfile by remember { mutableStateOf<UserProfile?>(null) }
                var profileMessage by rememberSaveable { mutableStateOf("") }
                var isProfileSaving by rememberSaveable { mutableStateOf(false) }
                var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
                var notificationsError by rememberSaveable { mutableStateOf("") }
                var isNotificationsLoading by rememberSaveable { mutableStateOf(false) }
                var unreadNotificationsCount by rememberSaveable { mutableStateOf(0) }

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
                                    currentUserProfile = authManager.getCurrentUserProfile()

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
                                    currentUserRole = "citizen"
                                    currentUserProfile = authManager.getCurrentUserProfile()

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

                    "Mapa" -> {
                        LaunchedEffect(Unit) {
                            try {
                                adminReportsError = ""
                                isAdminReportsLoading = true
                                allReports = reportRepository.getApprovedReports()
                            } catch (e: Exception) {
                                adminReportsError = e.message ?: "No se pudieron cargar los reportes."
                            } finally {
                                isAdminReportsLoading = false
                            }
                        }

                        MapScreen(
                            alerts = allReports.toMapAlerts(),
                            onNavigate = { selectedScreen = it },
                            onOpenAlert = { alert ->
                            selectedReport = alert.toUrbanReport()
                            publicDetailBackScreen = "Mapa"
                                selectedScreen = "DetallePublico"
                            }
                        )
                    }

                    "Detalle" -> DetailAlertScreen(
                        report = selectedReport,
                        canEdit = selectedReport?.status == "pending" || selectedReport?.status == "rejected",
                        canDelete = selectedReport?.id?.isNotBlank() == true,
                        isDeleting = isDeleteReportLoading,
                        onBack = {
                            selectedScreen = "Alertas"
                        },
                        onSafeRoute = {
                            selectedScreen = "PlanearRuta"
                        },
                        onEditReport = {
                            val reportToEdit = selectedReport

                            if (reportToEdit?.id?.isNotBlank() == true && (reportToEdit.status == "pending" || reportToEdit.status == "rejected")) {
                                editingReportId = reportToEdit.id
                                reportType = reportToEdit.type
                                reportUrgency = reportToEdit.urgency
                                reportDescription = reportToEdit.description
                                reportLocationName = reportToEdit.locationName
                                reportLatitude = reportToEdit.latitude
                                reportLongitude = reportToEdit.longitude
                                reportErrorMessage = ""

                                selectedScreen = "EditarReporte"
                            }
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

                    "DetalleAdmin" -> DetailAlertScreen(
                        report = selectedReport,
                        canEdit = false,
                        canDelete = false,
                        canReview = selectedReport?.status == "pending",
                        isDeleting = false,
                        isReviewing = isReviewReportLoading,
                        onBack = {
                            selectedScreen = "ListaAlertas"
                        },
                        onSafeRoute = {
                            selectedScreen = "PlanearRuta"
                        },
                        onApproveReport = {
                            val reportToReview = selectedReport

                            if (reportToReview?.id?.isNotBlank() == true) {
                                scope.launch {
                                    try {
                                        isReviewReportLoading = true
                                        adminReportsError = ""

                                        reportRepository.updateReportStatus(
                                            reportId = reportToReview.id,
                                            status = "approved"
                                        )

                                        try {
                                            notificationRepository.createReportStatusNotification(
                                                recipientId = reportToReview.userId,
                                                reportId = reportToReview.id,
                                                reportType = reportToReview.type,
                                                status = "approved"
                                            )
                                        } catch (_: Exception) {
                                        }

                                        scope.launch {
                                        try {
                                            EmailNotificationService.sendReportStatusEmail(reportToReview, "approved")
                                        } catch (_: Exception) {
                                        }
                                        }

                                        val updatedReport = reportToReview.copy(status = "approved", rejectionReason = "")
                                        selectedReport = updatedReport
                                        allReports = allReports.map { report ->
                                            if (report.id == updatedReport.id) updatedReport else report
                                        }
                                    } catch (e: Exception) {
                                        adminReportsError = e.message ?: "No se pudo validar el reporte."
                                    } finally {
                                        isReviewReportLoading = false
                                    }
                                }
                            }
                        },
                        onRejectReport = { rejectionReason ->
                            val reportToReview = selectedReport

                            if (reportToReview?.id?.isNotBlank() == true) {
                                scope.launch {
                                    try {
                                        isReviewReportLoading = true
                                        adminReportsError = ""

                                        reportRepository.rejectReport(
                                            reportId = reportToReview.id,
                                            reason = rejectionReason
                                        )

                                        try {
                                            notificationRepository.createReportStatusNotification(
                                                recipientId = reportToReview.userId,
                                                reportId = reportToReview.id,
                                                reportType = reportToReview.type,
                                                status = "rejected",
                                                rejectionReason = rejectionReason
                                            )
                                        } catch (_: Exception) {
                                        }

                                        scope.launch {
                                        try {
                                            EmailNotificationService.sendReportStatusEmail(reportToReview, "rejected", rejectionReason)
                                        } catch (_: Exception) {
                                        }
                                        }

                                        val updatedReport = reportToReview.copy(
                                            status = "rejected",
                                            rejectionReason = rejectionReason
                                        )
                                        selectedReport = updatedReport
                                        allReports = allReports.map { report ->
                                            if (report.id == updatedReport.id) updatedReport else report
                                        }
                                    } catch (e: Exception) {
                                        adminReportsError = e.message ?: "No se pudo rechazar el reporte."
                                    } finally {
                                        isReviewReportLoading = false
                                    }
                                }
                            }
                        }
                    )

                    "DetallePublico" -> DetailAlertScreen(
                        report = selectedReport,
                        canEdit = false,
                        canDelete = false,
                        canReview = false,
                        isDeleting = false,
                        onBack = {
                            selectedScreen = publicDetailBackScreen
                        },
                        onSafeRoute = {
                            selectedScreen = "PlanearRuta"
                        }
                    )

                    "Reportar" -> CreateReportScreen(
                        onNavigate = { selectedScreen = it },
                        onSelectLocation = {
                            locationReturnScreen = "Reportar"
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

                                    val createdReportId = reportRepository.createReport(
                                        UrbanReport(
                                            type = type,
                                            description = description,
                                            urgency = urgency,
                                            locationName = locationName,
                                            latitude = reportLatitude,
                                            longitude = reportLongitude,
                                            status = "pending",
                                            rejectionReason = ""
                                        )
                                    )

                                    try {
                                        notificationRepository.createReportSubmittedNotification(
                                            reportId = createdReportId,
                                            reportType = type,
                                            userName = currentUserProfile?.name.orEmpty()
                                        )
                                    } catch (_: Exception) {
                                    }

                                    scope.launch {
                                    try {
                                        EmailNotificationService.sendAdminNewReportEmail(
                                            reportType = type,
                                            userName = currentUserProfile?.name.orEmpty()
                                        )
                                    } catch (_: Exception) {
                                    }
                                    }

                                    reportType = "Bache"
                                    reportUrgency = "Media"
                                    reportDescription = ""
                                    reportLocationName = "Av. Independencia 250, Col. Centro"
                                    reportLatitude = null
                                    reportLongitude = null

                                    selectedScreen = "Alertas"
                                } catch (e: Exception) {
                                    reportErrorMessage = e.message ?: "No se pudo crear el reporte."
                                } finally {
                                    isReportLoading = false
                                }

                            }
                        }
                    )

                    "EditarReporte" -> CreateReportScreen(
                        onNavigate = { selectedScreen = it },
                        onSelectLocation = {
                            locationReturnScreen = "EditarReporte"
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
                        screenTitle = "Editar reporte",
                        screenSubtitle = if (selectedReport?.status == "rejected") "Corrige el reporte para enviarlo nuevamente a revision." else "Actualiza la informacion mientras el reporte esta pendiente.",
                        submitButtonText = if (selectedReport?.status == "rejected") "Reenviar reporte" else "Guardar cambios",
                        isLoading = isReportLoading,
                        errorMessage = reportErrorMessage,
                        onSubmitReport = { type, description, urgency, locationName ->
                            scope.launch {
                                try {
                                    reportErrorMessage = ""
                                    isReportLoading = true

                                    reportRepository.updateReport(
                                        reportId = editingReportId,
                                        type = type,
                                        description = description,
                                        urgency = urgency,
                                        locationName = locationName,
                                        latitude = reportLatitude,
                                        longitude = reportLongitude
                                    )

                                    val updatedReport = selectedReport?.copy(
                                        type = type,
                                        description = description,
                                        urgency = urgency,
                                        locationName = locationName,
                                        latitude = reportLatitude,
                                        longitude = reportLongitude,
                                        status = "pending",
                                        rejectionReason = ""
                                    )

                                    if (updatedReport != null) {
                                        selectedReport = updatedReport
                                        myReports = myReports.map { report ->
                                            if (report.id == updatedReport.id) {
                                                updatedReport
                                            } else {
                                                report
                                            }
                                        }
                                    }

                                    editingReportId = ""
                                    reportType = "Bache"
                                    reportUrgency = "Media"
                                    reportDescription = ""
                                    reportLocationName = "Av. Independencia 250, Col. Centro"
                                    reportLatitude = null
                                    reportLongitude = null

                                    selectedScreen = "Detalle"
                                } catch (e: Exception) {
                                    reportErrorMessage = e.message ?: "No se pudo actualizar el reporte."
                                } finally {
                                    isReportLoading = false
                                }
                            }
                        }
                    )

                    "SeleccionarUbicacion" -> {
                        LaunchedEffect(Unit) {
                            try {
                                adminReportsError = ""
                                isAdminReportsLoading = true
                                allReports = reportRepository.getApprovedReports()
                            } catch (e: Exception) {
                                adminReportsError = e.message ?: "No se pudieron cargar los reportes."
                            } finally {
                                isAdminReportsLoading = false
                            }
                        }

                        SelectLocationScreen(
                            initialAddress = reportLocationName,
                        nearbyAlerts = allReports.toMapAlerts(),
                        onBack = {
                            selectedScreen = locationReturnScreen
                        },
                        onConfirm = { selectedAddress, selectedCoordinate ->
                            reportLocationName = selectedAddress
                            reportLatitude = selectedCoordinate.latitude
                            reportLongitude = selectedCoordinate.longitude
                            selectedScreen = locationReturnScreen
                            }
                        )
                    }

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

                    "IA" -> {
                        LaunchedEffect(Unit) {
                            try {
                                adminReportsError = ""
                                isAdminReportsLoading = true
                                allReports = reportRepository.getApprovedReports()
                            } catch (e: Exception) {
                                adminReportsError = e.message ?: "No se pudieron cargar los reportes."
                            } finally {
                                isAdminReportsLoading = false
                            }
                        }

                        AIRecommendationsScreen(
                            reports = allReports,
                            onBack = {
                                selectedScreen = "Inicio"
                            },
                            onNavigate = {
                                selectedScreen = it
                            }
                        )
                    }

                    "Notificaciones" -> {
                        LaunchedEffect(Unit) {
                            try {
                                notificationsError = ""
                                isNotificationsLoading = true
                                notifications = notificationRepository.getMyNotifications(currentUserRole)
                                unreadNotificationsCount = notifications.count { !it.read }
                            } catch (e: Exception) {
                                notificationsError = e.message ?: "No se pudieron cargar tus notificaciones."
                            } finally {
                                isNotificationsLoading = false
                            }
                        }

                        NotificationsScreen(
                            notifications = notifications,
                            isLoading = isNotificationsLoading,
                            errorMessage = notificationsError,
                            onBack = {
                                selectedScreen = notificationsBackScreen
                            },
                            onMarkAsRead = { notification ->
                                scope.launch {
                                    try {
                                        notificationRepository.markAsRead(notification.id)
                                        notifications = notifications.map {
                                            if (it.id == notification.id) it.copy(read = true) else it
                                        }
                                        unreadNotificationsCount = notifications.count { !it.read }
                                    } catch (e: Exception) {
                                        notificationsError = e.message ?: "No se pudo actualizar la notificacion."
                                    }
                                }
                            }
                        )
                    }
                    "Perfil" -> {
                        LaunchedEffect(currentUserRole) {
                            try {
                                val loadedNotifications = notificationRepository.getMyNotifications(currentUserRole)
                                unreadNotificationsCount = loadedNotifications.count { !it.read }
                            } catch (_: Exception) {
                            }
                        }

                        if (currentUserRole == "admin") {
                            AdminProfileScreen(
                                profile = currentUserProfile ?: UserProfile(role = "admin"),
                                isSaving = isProfileSaving,
                                message = profileMessage,
                                unreadNotificationsCount = unreadNotificationsCount,
                                onNavigate = {
                                    if (it == "Notificaciones") {
                                        notificationsBackScreen = "Perfil"
                                    }
                                    selectedScreen = it
                                },
                                onUpdateProfile = { name, email, password ->
                                    scope.launch {
                                        try {
                                            profileMessage = ""
                                            isProfileSaving = true
                                            currentUserProfile = authManager.updateCurrentProfile(
                                                name = name,
                                                email = email,
                                                currentPassword = password
                                            )
                                            profileMessage = "Perfil actualizado correctamente."
                                        } catch (e: Exception) {
                                            profileMessage = e.message ?: "No se pudo actualizar el perfil."
                                        } finally {
                                            isProfileSaving = false
                                        }
                                    }
                                },
                                onChangePassword = { currentPassword, newPassword ->
                                    scope.launch {
                                        try {
                                            profileMessage = ""
                                            isProfileSaving = true
                                            authManager.changeCurrentPassword(
                                                currentPassword = currentPassword,
                                                newPassword = newPassword
                                            )
                                            profileMessage = "Contrasena actualizada correctamente."
                                        } catch (e: Exception) {
                                            profileMessage = e.message ?: "No se pudo cambiar la contrasena."
                                        } finally {
                                            isProfileSaving = false
                                        }
                                    }
                                },
                                onLogout = {
                                    authManager.logout()
                                    currentUserRole = "citizen"
                                    currentUserProfile = null
                                    profileMessage = ""
                                    selectedScreen = "Bienvenida"
                                }
                            )
                        } else {
                            ProfileScreen(
                                profile = currentUserProfile ?: UserProfile(),
                                isSaving = isProfileSaving,
                                message = profileMessage,
                                unreadNotificationsCount = unreadNotificationsCount,
                                onNavigate = {
                                    if (it == "Notificaciones") {
                                        notificationsBackScreen = "Perfil"
                                    }
                                    selectedScreen = it
                                },
                                onUpdateProfile = { name, email, password ->
                                    scope.launch {
                                        try {
                                            profileMessage = ""
                                            isProfileSaving = true
                                            currentUserProfile = authManager.updateCurrentProfile(
                                                name = name,
                                                email = email,
                                                currentPassword = password
                                            )
                                            profileMessage = "Perfil actualizado correctamente."
                                        } catch (e: Exception) {
                                            profileMessage = e.message ?: "No se pudo actualizar el perfil."
                                        } finally {
                                            isProfileSaving = false
                                        }
                                    }
                                },
                                onChangePassword = { currentPassword, newPassword ->
                                    scope.launch {
                                        try {
                                            profileMessage = ""
                                            isProfileSaving = true
                                            authManager.changeCurrentPassword(
                                                currentPassword = currentPassword,
                                                newPassword = newPassword
                                            )
                                            profileMessage = "Contraseña actualizada correctamente."
                                        } catch (e: Exception) {
                                            profileMessage = e.message ?: "No se pudo cambiar la contraseña."
                                        } finally {
                                            isProfileSaving = false
                                        }
                                    }
                                },
                                onLogout = {
                                    authManager.logout()
                                    currentUserRole = "citizen"
                                    currentUserProfile = null
                                    unreadNotificationsCount = 0
                                    profileMessage = ""
                                    selectedScreen = "Bienvenida"
                                }
                            )
                        }
                    }

                    "Administrador" -> {
                        LaunchedEffect(Unit) {
                            try {
                                adminReportsError = ""
                                isAdminReportsLoading = true
                                allReports = reportRepository.getAllReports()
                            } catch (e: Exception) {
                                adminReportsError = e.message ?: "No se pudieron cargar los reportes."
                            } finally {
                                isAdminReportsLoading = false
                            }
                        }

                        AdminPanelScreen(
                            reports = allReports,
                            isLoading = isAdminReportsLoading,
                            errorMessage = adminReportsError,
                            onBack = {
                                selectedScreen = "Perfil"
                            },
                            onOpenReport = { report ->
                                selectedReport = report
                                selectedScreen = "DetalleAdmin"
                            },
                            onNavigate = {
                                selectedScreen = it
                            }
                        )
                    }

                    "ListaAlertas" -> {
                        LaunchedEffect(Unit) {
                            try {
                                adminReportsError = ""
                                isAdminReportsLoading = true
                                allReports = reportRepository.getAllReports()
                            } catch (e: Exception) {
                                adminReportsError = e.message ?: "No se pudieron cargar los reportes."
                            } finally {
                                isAdminReportsLoading = false
                            }
                        }

                        AlertsListScreen(
                            reports = allReports,
                            isLoading = isAdminReportsLoading,
                            errorMessage = adminReportsError,
                            onBack = {
                                selectedScreen = "Administrador"
                            },
                            onOpenDetail = { report ->
                                selectedReport = report
                                selectedScreen = "DetalleAdmin"
                            },
                            onNavigate = {
                                selectedScreen = it
                            }
                        )
                    }

                    else -> {
                        LaunchedEffect(Unit) {
                            try {
                                adminReportsError = ""
                                isAdminReportsLoading = true
                                allReports = reportRepository.getApprovedReports()
                            } catch (e: Exception) {
                                adminReportsError = e.message ?: "No se pudieron cargar los reportes."
                            } finally {
                                isAdminReportsLoading = false
                            }
                        }

                        HomeScreen(
                            userName = currentUserProfile?.name.orEmpty(),
                            reports = allReports,
                            isLoading = isAdminReportsLoading,
                            errorMessage = adminReportsError,
                            unreadNotificationsCount = unreadNotificationsCount,
                            onOpenReport = { report ->
                                selectedReport = report
                                selectedScreen = "DetallePublico"
                            },
                            onOpenNotifications = {
                                notificationsBackScreen = "Inicio"
                                selectedScreen = "Notificaciones"
                            },
                            onNavigate = { selectedScreen = it }
                        )
                    }
                }
            }
        }
    }
}
private fun List<UrbanReport>.toMapAlerts(): List<UrbanAlert> {
    return filter { report ->
        report.status == "approved" && report.latitude != null && report.longitude != null
    }.map { report ->
        UrbanAlert(
            id = report.id,
            title = report.type.ifBlank { "Reporte urbano" },
            category = report.type.ifBlank { "Reporte" },
            address = report.locationName.ifBlank { "Sin ubicación" },
            description = report.description,
            urgency = report.urgency.ifBlank { "Media" },
            distanceText = "Ubicación reportada",
            timeText = "Reporte validado",
            latitude = report.latitude ?: 0.0,
            longitude = report.longitude ?: 0.0
        )
    }
}

private fun UrbanAlert.toUrbanReport(): UrbanReport {
    return UrbanReport(
        id = id,
        type = title,
        description = description,
        urgency = urgency,
        locationName = address,
        latitude = latitude,
        longitude = longitude,
        status = "approved",
        userName = "Reporte ciudadano",
        createdAt = System.currentTimeMillis()
    )
}



