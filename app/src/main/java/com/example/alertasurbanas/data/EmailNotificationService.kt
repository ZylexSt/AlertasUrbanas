package com.example.alertasurbanas.data

import com.example.alertasurbanas.BuildConfig
import com.example.alertasurbanas.model.UrbanReport
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object EmailNotificationService {
    suspend fun sendReportStatusEmail(
        report: UrbanReport,
        status: String,
        rejectionReason: String = ""
    ) {
        val recipientEmail = resolveRecipientEmail(report)
        if (recipientEmail.isBlank()) return

        val payload = JSONObject().apply {
            put("toEmail", recipientEmail)
            put("toName", report.userName.ifBlank { "Usuario" })
            put("status", status)
            put("reportType", report.type)
            put("rejectionReason", rejectionReason)
        }

        postEmail(payload)
    }

    suspend fun sendAdminNewReportEmail(
        reportType: String,
        userName: String
    ) {
        val adminEmail = BuildConfig.ADMIN_NOTIFICATION_EMAIL.trim()
        if (adminEmail.isBlank()) return

        val reporterName = userName.ifBlank { "Un usuario" }
        val payload = JSONObject().apply {
            put("toEmail", adminEmail)
            put("toName", "Administrador")
            put("subject", "Nuevo reporte pendiente")
            put("title", "Nuevo reporte pendiente")
            put("message", "$reporterName envio un reporte de $reportType para revision.")
            put("reportType", reportType)
        }

        postEmail(payload)
    }

    private suspend fun resolveRecipientEmail(report: UrbanReport): String {
        val reportEmail = report.userEmail.trim()
        if (reportEmail.isNotBlank()) return reportEmail

        val userNameEmail = report.userName.trim().takeIf { it.contains("@") }.orEmpty()
        if (userNameEmail.isNotBlank()) return userNameEmail

        if (report.userId.isBlank()) return ""

        return runCatching {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(report.userId)
                .get()
                .await()
                .getString("email")
                .orEmpty()
                .trim()
        }.getOrDefault("")
    }

    private suspend fun postEmail(payload: JSONObject) = withContext(Dispatchers.IO) {
        val functionUrl = BuildConfig.SUPABASE_EMAIL_FUNCTION_URL.trim()
        if (functionUrl.isBlank()) return@withContext

        val connection = (URL(functionUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")

            val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()
            if (anonKey.isNotBlank()) {
                setRequestProperty("apikey", anonKey)
                setRequestProperty("Authorization", "Bearer $anonKey")
            }
        }

        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorMessage = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "No se pudo enviar el correo. Codigo HTTP: $responseCode"
                throw Exception(errorMessage)
            }
        } finally {
            connection.disconnect()
        }
    }
}