package com.example.alertasurbanas.data

import com.example.alertasurbanas.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.net.URLEncoder
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class MapSearchResult(
    val title: String,
    val subtitle: String,
    val coordinate: LatLng,
    val distanceKm: Double? = null
)

object MapSearchService {
    private const val DEFAULT_SEARCH_RADIUS_METERS = 20_000

    suspend fun searchFirstResult(
        query: String,
        proximity: LatLng? = null
    ): LatLng? {
        return searchResults(
            query = query,
            limit = 1,
            proximity = proximity
        ).firstOrNull()?.coordinate
    }

    suspend fun searchResults(
        query: String,
        limit: Int = 5,
        proximity: LatLng? = null,
        radiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS
    ): List<MapSearchResult> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank() || BuildConfig.GEOAPIFY_API_KEY.isBlank()) {
                return@withContext emptyList()
            }

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val locationParams = proximity?.let {
                "&filter=circle:${it.longitude},${it.latitude},$radiusMeters" +
                    "&bias=proximity:${it.longitude},${it.latitude}"
            } ?: ""

            val url = URL(
                "https://api.geoapify.com/v1/geocode/autocomplete" +
                    "?text=$encodedQuery" +
                    "&format=json" +
                    "&limit=$limit" +
                    "&lang=es" +
                    "&apiKey=${BuildConfig.GEOAPIFY_API_KEY}" +
                    locationParams
            )

            val response = url.openStream().bufferedReader().use { it.readText() }
            val resultsArray = JSONObject(response).getJSONArray("results")
            val results = mutableListOf<MapSearchResult>()

            for (index in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(index)
                val latitude = item.optDouble("lat", Double.NaN)
                val longitude = item.optDouble("lon", Double.NaN)

                if (latitude.isNaN() || longitude.isNaN()) continue

                val coordinate = LatLng(latitude, longitude)
                val distanceKm = proximity?.let { distanceInKm(it, coordinate) }

                results.add(
                    MapSearchResult(
                        title = buildTitle(item),
                        subtitle = buildSubtitle(item, distanceKm),
                        coordinate = coordinate,
                        distanceKm = distanceKm
                    )
                )
            }

            results
                .distinctBy { "${it.title}-${it.coordinate.latitude}-${it.coordinate.longitude}" }
                .sortedWith(compareBy<MapSearchResult> { it.distanceKm ?: Double.MAX_VALUE }.thenBy { it.title })
                .take(limit)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun reverseGeocode(coordinate: LatLng): String? = withContext(Dispatchers.IO) {
        try {
            if (BuildConfig.GEOAPIFY_API_KEY.isBlank()) return@withContext null

            val url = URL(
                "https://api.geoapify.com/v1/geocode/reverse" +
                    "?lat=${coordinate.latitude}" +
                    "&lon=${coordinate.longitude}" +
                    "&format=json" +
                    "&lang=es" +
                    "&apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
            )

            val response = url.openStream().bufferedReader().use { it.readText() }
            val results = JSONObject(response).getJSONArray("results")

            if (results.length() == 0) null else {
                val item = results.getJSONObject(0)
                item.optString("formatted").ifBlank { buildSubtitle(item, null) }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildTitle(item: JSONObject): String {
        return item.optString("name").ifBlank {
            item.optString("street").ifBlank {
                item.optString("city").ifBlank {
                    item.optString("formatted").ifBlank { "Ubicación" }
                }
            }
        }
    }

    private fun buildSubtitle(item: JSONObject, distanceKm: Double?): String {
        val formatted = item.optString("formatted")
        val city = item.optString("city")
        val state = item.optString("state")
        val distanceText = distanceKm?.let {
            if (it < 1.0) "${(it * 1000).toInt()} m" else "${String.format("%.1f", it)} km"
        }

        val base = formatted.ifBlank {
            listOf(city, state)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .ifBlank { "Resultado cercano" }
        }

        return if (distanceText != null) "$base · $distanceText" else base
    }

    private fun distanceInKm(from: LatLng, to: LatLng): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val fromLat = Math.toRadians(from.latitude)
        val toLat = Math.toRadians(to.latitude)

        val a = sin(dLat / 2).pow(2.0) + cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
