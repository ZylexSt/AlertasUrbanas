package com.example.alertasurbanas.data

import com.example.alertasurbanas.model.UrbanReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val reportsCollection = db.collection("reports")

    suspend fun createReport(report: UrbanReport): String {
        val currentUser = auth.currentUser
            ?: throw Exception("Debes iniciar sesion para crear un reporte")

        val reportToSave = report.copy(
            userId = currentUser.uid,
            userName = currentUser.displayName ?: currentUser.email.orEmpty(),
            status = "pending",
            createdAt = System.currentTimeMillis()
        )

        val document = reportsCollection.add(reportToSave).await()
        return document.id
    }
    suspend fun getAllReports(): List<UrbanReport> {
        return reportsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(UrbanReport::class.java)?.copy(id = document.id)
            }
    }


    suspend fun getApprovedReports(): List<UrbanReport> {
        return reportsCollection
            .whereEqualTo("status", "approved")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(UrbanReport::class.java)?.copy(id = document.id)
            }
            .sortedByDescending { report -> report.createdAt }
    }
    suspend fun getMyReports(): List<UrbanReport> {
        val currentUser = auth.currentUser
            ?: throw Exception("Debes iniciar sesion para ver tus reportes")

        return reportsCollection
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(UrbanReport::class.java)?.copy(id = document.id)
            }
    }



    suspend fun updateReport(
        reportId: String,
        type: String,
        description: String,
        urgency: String,
        locationName: String,
        latitude: Double?,
        longitude: Double?
    ) {
        reportsCollection
            .document(reportId)
            .update(
                mapOf(
                    "type" to type,
                    "description" to description,
                    "urgency" to urgency,
                    "locationName" to locationName,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "status" to "pending",
                    "rejectionReason" to ""
                )
            )
            .await()
    }

    suspend fun updateReportStatus(
        reportId: String,
        status: String
    ) {
        val updates = mutableMapOf<String, Any>("status" to status)
        if (status == "approved") {
            updates["rejectionReason"] = ""
        }

        reportsCollection
            .document(reportId)
            .update(updates)
            .await()
    }

    suspend fun rejectReport(
        reportId: String,
        reason: String
    ) {
        reportsCollection
            .document(reportId)
            .update(
                mapOf(
                    "status" to "rejected",
                    "rejectionReason" to reason
                )
            )
            .await()
    }

    suspend fun deleteReport(reportId: String) {
        reportsCollection
            .document(reportId)
            .delete()
            .await()
    }
}



