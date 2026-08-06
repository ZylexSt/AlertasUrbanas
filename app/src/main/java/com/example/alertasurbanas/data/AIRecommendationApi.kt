package com.example.alertasurbanas.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.WarningAmber
import com.example.alertasurbanas.BuildConfig
import com.example.alertasurbanas.model.UrbanReport
import com.example.alertasurbanas.ui.theme.UrbanColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AIRecommendationApi {
    suspend fun getAlertRecommendation(
        report: UrbanReport,
        reports: List<UrbanReport>
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = BuildConfig.AI_API_BASE_URL.trimEnd('/')
            if (baseUrl.isBlank()) return@withContext null

            val connection = (URL("$baseUrl/alert-recommendation").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 6_000
                readTimeout = 8_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().apply {
                put("selectedReport", report.toJson())
                put(
                    "reports",
                    JSONArray().apply {
                        reports.forEach { item ->
                            put(item.toJson())
                        }
                    }
                )
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            connection.disconnect()

            if (responseCode !in 200..299 || responseText.isBlank()) {
                return@runCatching null
            }

            JSONObject(responseText).optString("recommendation").takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    suspend fun getRecommendations(reports: List<UrbanReport>): AIRiskSummary? = withContext(Dispatchers.IO) {
        runCatching {
            val baseUrl = BuildConfig.AI_API_BASE_URL.trimEnd('/')
            if (baseUrl.isBlank()) return@withContext null

            val connection = (URL("$baseUrl/recommendations").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 6_000
                readTimeout = 8_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().apply {
                put(
                    "reports",
                    JSONArray().apply {
                        reports.forEach { report ->
                            put(
                                JSONObject().apply {
                                    put("id", report.id)
                                    put("type", report.type)
                                    put("description", report.description)
                                    put("urgency", report.urgency)
                                    put("locationName", report.locationName)
                                    put("status", report.status)
                                    put("createdAt", report.createdAt)

                                    if (report.latitude != null) {
                                        put("latitude", report.latitude)
                                    }

                                    if (report.longitude != null) {
                                        put("longitude", report.longitude)
                                    }
                                }
                            )
                        }
                    }
                )
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            connection.disconnect()

            if (responseCode !in 200..299 || responseText.isBlank()) {
                return@runCatching null
            }

            parseSummary(JSONObject(responseText))
        }.getOrNull()
    }

    private fun UrbanReport.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("type", type)
            put("description", description)
            put("urgency", urgency)
            put("locationName", locationName)
            put("status", status)
            put("createdAt", createdAt)

            if (latitude != null) {
                put("latitude", latitude)
            }

            if (longitude != null) {
                put("longitude", longitude)
            }
        }
    }

    private fun parseSummary(json: JSONObject): AIRiskSummary {
        val recommendationsJson = json.optJSONArray("recommendations") ?: JSONArray()
        val recommendations = buildList {
            for (index in 0 until recommendationsJson.length()) {
                val item = recommendationsJson.optJSONObject(index) ?: continue

                add(
                    AIRecommendationResult(
                        title = item.optString("title"),
                        description = item.optString("description"),
                        label = item.optString("label"),
                        color = when (item.optString("colorKey")) {
                            "high" -> UrbanColors.HighUrgency
                            "medium" -> UrbanColors.MediumUrgency
                            "low" -> UrbanColors.PrimaryLight
                            "terracotta" -> UrbanColors.Terracotta
                            else -> UrbanColors.Primary
                        },
                        icon = when (item.optString("iconKey")) {
                            "warning" -> Icons.Outlined.WarningAmber
                            "route" -> Icons.Outlined.Route
                            "graph" -> Icons.Outlined.AutoGraph
                            else -> Icons.Outlined.Lightbulb
                        }
                    )
                )
            }
        }

        val zonesJson = json.optJSONArray("zones") ?: JSONArray()
        val zones = buildList {
            for (index in 0 until zonesJson.length()) {
                val item = zonesJson.optJSONObject(index) ?: continue
                val latitude = item.optDouble("latitude", Double.NaN)
                val longitude = item.optDouble("longitude", Double.NaN)

                if (latitude.isNaN() || longitude.isNaN()) continue

                add(
                    AIRiskZone(
                        id = item.optString("id", "zone-$index"),
                        title = item.optString("title", "Zona de riesgo"),
                        riskLevel = item.optString("riskLevel", "Media"),
                        dominantCategory = item.optString("dominantCategory", "Reportes"),
                        reportCount = item.optInt("reportCount", 0),
                        latitude = latitude,
                        longitude = longitude,
                        recommendation = item.optString("recommendation", "Revisa rutas alternativas cercanas.")
                    )
                )
            }
        }

        return AIRiskSummary(
            title = json.optString("title"),
            description = json.optString("description"),
            analyzedZones = json.optInt("analyzedZones"),
            highRiskZones = json.optInt("highRiskZones"),
            mediumRiskZones = json.optInt("mediumRiskZones"),
            lowRiskZones = json.optInt("lowRiskZones"),
            zones = zones,
            recommendations = recommendations.ifEmpty {
                AIRecommendationEngine.analyze(emptyList()).recommendations
            }
        )
    }
}
