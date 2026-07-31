package com.example.alertasurbanas.data

import com.example.alertasurbanas.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.cos

data class MapRouteResult(
    val points: List<LatLng>,
    val distanceKm: Double,
    val durationMinutes: Int,
    val nearbyReports: Int = 0
) {
    val summaryText: String
        get() = "${durationMinutes} min · ${String.format("%.1f", distanceKm)} km"

    fun withNearbyReports(count: Int): MapRouteResult {
        return copy(nearbyReports = count)
    }
}

object MapRouteService {
    suspend fun drivingRoute(
        origin: LatLng,
        destination: LatLng
    ): MapRouteResult? = withContext(Dispatchers.IO) {
        drivingRoutes(origin, destination, targetCount = 1).firstOrNull()
    }

    suspend fun drivingRoutes(
        origin: LatLng,
        destination: LatLng,
        targetCount: Int = 3
    ): List<MapRouteResult> = withContext(Dispatchers.IO) {
        val routes = mutableListOf<MapRouteResult>()
        routes += routesWithOpenRouteService(origin, destination, targetCount)

        if (routes.size < targetCount) {
            routeWithGeoapify(origin, destination)?.let { routes += it }
        }

        if (routes.size < targetCount) {
            routes += geoapifyAlternativeRoutes(origin, destination, targetCount - routes.size)
        }

        routes
            .filter { it.points.size >= 2 }
            .distinctBy { routeSignature(it) }
            .sortedWith(compareBy<MapRouteResult> { it.durationMinutes }.thenBy { it.distanceKm })
            .take(targetCount)
    }

    private fun routeWithOpenRouteService(
        origin: LatLng,
        destination: LatLng
    ): MapRouteResult? {
        return routesWithOpenRouteService(origin, destination, targetCount = 1).firstOrNull()
    }

    private fun routesWithOpenRouteService(
        origin: LatLng,
        destination: LatLng,
        targetCount: Int
    ): List<MapRouteResult> {
        return try {
            if (BuildConfig.ORS_API_KEY.isBlank()) return emptyList()

            val connection = (URL("https://api.openrouteservice.org/v2/directions/driving-car/geojson")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 12_000
                doOutput = true
                setRequestProperty("Authorization", BuildConfig.ORS_API_KEY)
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val body = """
                {
                  "coordinates": [
                    [${origin.longitude}, ${origin.latitude}],
                    [${destination.longitude}, ${destination.latitude}]
                  ],
                  "alternative_routes": {
                    "target_count": $targetCount,
                    "share_factor": 0.6,
                    "weight_factor": 1.6
                  }
                }
            """.trimIndent()

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
            }

            val response = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: return emptyList()
            }

            val features = JSONObject(response).getJSONArray("features")
            val routes = mutableListOf<MapRouteResult>()

            for (featureIndex in 0 until features.length()) {
                val feature = features.getJSONObject(featureIndex)
                val coordinates = feature
                    .getJSONObject("geometry")
                    .getJSONArray("coordinates")

                val points = buildList {
                    for (index in 0 until coordinates.length()) {
                        val coordinate = coordinates.getJSONArray(index)
                        add(LatLng(coordinate.getDouble(1), coordinate.getDouble(0)))
                    }
                }

                val summary = feature
                    .getJSONObject("properties")
                    .getJSONObject("summary")

                routes.add(
                    MapRouteResult(
                        points = points,
                        distanceKm = summary.optDouble("distance", 0.0) / 1000.0,
                        durationMinutes = (summary.optDouble("duration", 0.0) / 60.0).toInt().coerceAtLeast(1)
                    )
                )
            }

            routes
                .filter { it.points.size >= 2 }
                .sortedWith(compareBy<MapRouteResult> { it.nearbyReports }.thenBy { it.durationMinutes })
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun routeWithGeoapify(
        origin: LatLng,
        destination: LatLng,
        via: LatLng? = null
    ): MapRouteResult? {
        return try {
            if (BuildConfig.GEOAPIFY_API_KEY.isBlank()) return null

            val waypoints = buildString {
                append("${origin.latitude},${origin.longitude}")
                via?.let { append("|${it.latitude},${it.longitude}") }
                append("|${destination.latitude},${destination.longitude}")
            }

            val url = URL(
                "https://api.geoapify.com/v1/routing" +
                    "?waypoints=$waypoints" +
                    "&mode=drive" +
                    "&apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
            )

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode !in 200..299) return null

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val feature = JSONObject(response)
                .getJSONArray("features")
                .getJSONObject(0)

            val properties = feature.getJSONObject("properties")
            val geometry = feature.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")
            val points = mutableListOf<LatLng>()

            collectGeoapifyCoordinates(coordinates, points)

            MapRouteResult(
                points = points,
                distanceKm = properties.optDouble("distance", 0.0) / 1000.0,
                durationMinutes = (properties.optDouble("time", 0.0) / 60.0).toInt().coerceAtLeast(1)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun geoapifyAlternativeRoutes(
        origin: LatLng,
        destination: LatLng,
        needed: Int
    ): List<MapRouteResult> {
        if (BuildConfig.GEOAPIFY_API_KEY.isBlank() || needed <= 0) return emptyList()

        val midLatitude = (origin.latitude + destination.latitude) / 2.0
        val midLongitude = (origin.longitude + destination.longitude) / 2.0
        val dLat = destination.latitude - origin.latitude
        val dLon = destination.longitude - origin.longitude
        val lonScale = cos(Math.toRadians(midLatitude)).coerceAtLeast(0.25)

        val normalizedLat = -dLon * lonScale
        val normalizedLon = dLat / lonScale
        val length = kotlin.math.sqrt(normalizedLat * normalizedLat + normalizedLon * normalizedLon)
            .takeIf { it > 0.00001 }
            ?: return emptyList()

        val unitLat = normalizedLat / length
        val unitLon = normalizedLon / length
        val offsets = listOf(0.018, -0.018, 0.032, -0.032)

        return offsets.mapNotNull { offset ->
            val via = LatLng(
                midLatitude + unitLat * offset,
                midLongitude + unitLon * offset
            )
            routeWithGeoapify(origin, destination, via)
        }.take(needed)
    }

    private fun routeSignature(route: MapRouteResult): String {
        val start = route.points.firstOrNull()
        val middle = route.points.getOrNull(route.points.size / 2)
        val end = route.points.lastOrNull()

        return listOf(start, middle, end).joinToString("|") { point ->
            if (point == null) {
                "none"
            } else {
                "${String.format("%.4f", point.latitude)},${String.format("%.4f", point.longitude)}"
            }
        }
    }

    private fun collectGeoapifyCoordinates(
        coordinates: org.json.JSONArray,
        points: MutableList<LatLng>
    ) {
        for (index in 0 until coordinates.length()) {
            val item = coordinates.get(index)

            if (item is org.json.JSONArray) {
                val first = item.opt(0)
                val second = item.opt(1)

                if (first is Number && second is Number && item.length() >= 2) {
                    points.add(LatLng(second.toDouble(), first.toDouble()))
                } else {
                    collectGeoapifyCoordinates(item, points)
                }
            }
        }
    }
}
