package com.example.alertasurbanas.data

import com.example.alertasurbanas.model.AppNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val notificationsCollection = db.collection("notifications")

    suspend fun getMyNotifications(userRole: String): List<AppNotification> {
        val currentUser = auth.currentUser
            ?: throw Exception("Debes iniciar sesion para ver tus notificaciones")

        val recipientIds = if (userRole == "admin") {
            listOf(currentUser.uid, ADMIN_RECIPIENT_ID)
        } else {
            listOf(currentUser.uid)
        }

        return recipientIds
            .flatMap { recipientId ->
                notificationsCollection
                    .whereEqualTo("recipientId", recipientId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { document ->
                        document.toObject(AppNotification::class.java)?.copy(id = document.id)
                    }
            }
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
    }

    suspend fun createReportSubmittedNotification(
        reportId: String,
        reportType: String,
        userName: String
    ) {
        notificationsCollection.add(
            AppNotification(
                recipientId = ADMIN_RECIPIENT_ID,
                title = "Nuevo reporte pendiente",
                message = "${userName.ifBlank { "Un usuario" }} envio un reporte de $reportType para revision.",
                type = "report_created",
                reportId = reportId,
                read = false,
                createdAt = System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun createReportStatusNotification(
        recipientId: String,
        reportId: String,
        reportType: String,
        status: String,
        rejectionReason: String = ""
    ) {
        val approved = status == "approved"
        val title = if (approved) "Reporte validado" else "Reporte rechazado"
        val message = if (approved) {
            "Tu reporte de $reportType fue validado por administracion."
        } else {
            val reasonText = rejectionReason.ifBlank { "No se especifico un motivo." }
            "Tu reporte de $reportType fue rechazado. Motivo: $reasonText"
        }

        notificationsCollection.add(
            AppNotification(
                recipientId = recipientId,
                title = title,
                message = message,
                type = if (approved) "report_approved" else "report_rejected",
                reportId = reportId,
                read = false,
                createdAt = System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun markAsRead(notificationId: String) {
        notificationsCollection
            .document(notificationId)
            .update("read", true)
            .await()
    }

    companion object {
        const val ADMIN_RECIPIENT_ID = "admin"
    }
}