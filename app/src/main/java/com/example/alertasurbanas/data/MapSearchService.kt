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
    private const val DEFAULT_SEARCH_RADIUS_METERS = 0
    private const val DEFAULT_TAP_RADIUS_METERS = 120

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
        radiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
        showDistance: Boolean = true
    ): List<MapSearchResult> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank() || BuildConfig.GEOAPIFY_API_KEY.isBlank()) {
                return@withContext emptyList()
            }

            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val locationParams = proximity?.let {
                val filter = if (radiusMeters > 0) {
                    "&filter=circle:${it.longitude},${it.latitude},$radiusMeters"
                } else {
                    ""
                }
                filter + "&bias=proximity:${it.longitude},${it.latitude}"
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
                val visibleDistanceKm = if (showDistance) distanceKm else null

                results.add(
                    MapSearchResult(
                        title = buildTitle(item),
                        subtitle = buildSubtitle(item, visibleDistanceKm),
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

    suspend fun nearbyPlaceOrAddress(
        coordinate: LatLng,
        proximity: LatLng? = null,
        radiusMeters: Int = DEFAULT_TAP_RADIUS_METERS
    ): MapSearchResult? = withContext(Dispatchers.IO) {
        try {
            if (BuildConfig.GEOAPIFY_API_KEY.isBlank()) return@withContext null

            val categories = URLEncoder.encode(
                listOf(
                    "commercial",
                    "catering",
                    "education",
                    "healthcare",
                    "entertainment",
                    "tourism",
                    "leisure",
                    "service",
                    "public_transport",
                    "parking"
                ).joinToString(","),
                "UTF-8"
            )

            val url = URL(
                "https://api.geoapify.com/v2/places" +
                    "?categories=$categories" +
                    "&filter=circle:${coordinate.longitude},${coordinate.latitude},$radiusMeters" +
                    "&bias=proximity:${coordinate.longitude},${coordinate.latitude}" +
                    "&limit=1" +
                    "&lang=es" +
                    "&apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
            )

            val response = url.openStream().bufferedReader().use { it.readText() }
            val features = JSONObject(response).optJSONArray("features")

            if (features != null && features.length() > 0) {
                val feature = features.getJSONObject(0)
                val properties = feature.optJSONObject("properties") ?: JSONObject()
                val geometry = feature.optJSONObject("geometry")
                val coordinates = geometry?.optJSONArray("coordinates")
                val longitude = coordinates?.optDouble(0, Double.NaN) ?: Double.NaN
                val latitude = coordinates?.optDouble(1, Double.NaN) ?: Double.NaN
                val placeCoordinate = if (!latitude.isNaN() && !longitude.isNaN()) {
                    LatLng(latitude, longitude)
                } else {
                    coordinate
                }
                val distanceKm = proximity?.let { distanceInKm(it, placeCoordinate) }

                return@withContext MapSearchResult(
                    title = buildTitle(properties),
                    subtitle = buildSubtitle(properties, distanceKm),
                    coordinate = placeCoordinate,
                    distanceKm = distanceKm
                )
            }

            val address = reverseGeocode(coordinate)
            address?.let {
                MapSearchResult(
                    title = "Punto seleccionado",
                    subtitle = it,
                    coordinate = coordinate,
                    distanceKm = proximity?.let { origin -> distanceInKm(origin, coordinate) }
                )
            }
        } catch (_: Exception) {
            val address = reverseGeocode(coordinate)
            address?.let {
                MapSearchResult(
                    title = "Punto seleccionado",
                    subtitle = it,
                    coordinate = coordinate,
                    distanceKm = proximity?.let { origin -> distanceInKm(origin, coordinate) }
                )
            }
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
