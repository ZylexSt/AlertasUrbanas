package com.example.alertasurbanas.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng

data class CarSyncedRoute(
    val routes: List<List<LatLng>> = emptyList(),
    val selectedRouteIndex: Int = 0,
    val origin: LatLng? = null,
    val destination: LatLng? = null,
    val destinationName: String = "",
    val routeSummaries: List<String> = emptyList(),
    val nearbyReports: List<Int> = emptyList(),
    val selectedCategory: String = "Todas",
    val selectedUrgency: String = "Todas",
    val source: String = "",
    val syncType: String = "",
    val updatedAt: Long = 0L
)

class CarMapSyncRepository {
    private val auth = FirebaseAuth.getInstance()
    private val stateDocument = FirebaseFirestore
        .getInstance()
        .collection("car_map_state")
        .document("current")

    fun observeRoute(): Flow<CarSyncedRoute> = callbackFlow {
        try {
            ensureSyncSession()
        } catch (_: Exception) {
            // Si Firebase Auth anónimo no está habilitado, el listener puede fallar
            // según las reglas. La app no debe cerrarse por eso.
        }

        val listener = stateDocument.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(CarSyncedRoute())
                return@addSnapshotListener
            }

            trySend(snapshot?.data.toCarSyncedRoute())
        }

        awaitClose { listener.remove() }
    }

    suspend fun publishRoute(
        routes: List<MapRouteResult>,
        selectedRouteIndex: Int,
        origin: LatLng,
        destination: LatLng,
        destinationName: String
    ) {
        ensureSyncSession()
        val safeSelectedIndex = selectedRouteIndex.coerceIn(0, (routes.size - 1).coerceAtLeast(0))

        stateDocument.set(
            mapOf(
                "routes" to routes.map { route ->
                    mapOf(
                        "points" to route.points.map { point ->
                            mapOf(
                                "latitude" to point.latitude,
                                "longitude" to point.longitude
                            )
                        }
                    )
                },
                "selectedRouteIndex" to safeSelectedIndex,
                "origin" to mapOf(
                    "latitude" to origin.latitude,
                    "longitude" to origin.longitude
                ),
                "destination" to mapOf(
                    "latitude" to destination.latitude,
                    "longitude" to destination.longitude
                ),
                "destinationName" to destinationName,
                "routeSummaries" to routes.map { route ->
                    "${route.durationMinutes} min · ${String.format("%.1f", route.distanceKm)} km"
                },
                "nearbyReports" to routes.map { it.nearbyReports },
                "source" to "mobile",
                "syncType" to "active_route",
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun publishFilters(
        selectedCategory: String,
        selectedUrgency: String,
        source: String = "mobile"
    ) {
        ensureSyncSession()

        stateDocument.set(
            mapOf(
                "selectedCategory" to selectedCategory,
                "selectedUrgency" to selectedUrgency,
                "source" to source,
                "syncType" to "filters",
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun clearRoute(source: String = "mobile") {
        ensureSyncSession()
        stateDocument.set(
            mapOf(
                "routes" to emptyList<Map<String, List<Map<String, Double>>>>(),
                "selectedRouteIndex" to 0,
                "destinationName" to "",
                "routeSummaries" to emptyList<String>(),
                "nearbyReports" to emptyList<Int>(),
                "source" to source,
                "syncType" to "empty_route",
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    private suspend fun ensureSyncSession() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }
}

private fun Map<String, Any>?.toCarSyncedRoute(): CarSyncedRoute {
    if (this == null) return CarSyncedRoute()

    val routes = (this["routes"] as? List<*>)
        ?.mapNotNull { rawRoute ->
            when (rawRoute) {
                is Map<*, *> -> {
                    (rawRoute["points"] as? List<*>)
                        ?.mapNotNull { rawPoint ->
                            (rawPoint as? Map<*, *>).toLatLng()
                        }
                        ?.takeIf { it.size >= 2 }
                }

                is List<*> -> {
                    rawRoute.mapNotNull { rawPoint ->
                        (rawPoint as? Map<*, *>).toLatLng()
                    }.takeIf { it.size >= 2 }
                }

                else -> null
            }
        }
        .orEmpty()

    return CarSyncedRoute(
        routes = routes,
        selectedRouteIndex = (this["selectedRouteIndex"] as? Number)?.toInt()
            ?.coerceIn(0, (routes.size - 1).coerceAtLeast(0))
            ?: 0,
        origin = (this["origin"] as? Map<*, *>).toLatLng(),
        destination = (this["destination"] as? Map<*, *>).toLatLng(),
        destinationName = this["destinationName"] as? String ?: "",
        routeSummaries = (this["routeSummaries"] as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        nearbyReports = (this["nearbyReports"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }.orEmpty(),
        selectedCategory = this["selectedCategory"] as? String ?: "Todas",
        selectedUrgency = this["selectedUrgency"] as? String ?: "Todas",
        source = this["source"] as? String ?: "",
        syncType = this["syncType"] as? String ?: "",
        updatedAt = (this["updatedAt"] as? Number)?.toLong() ?: 0L
    )
}

private fun Map<*, *>?.toLatLng(): LatLng? {
    if (this == null) return null

    val latitude = this["latitude"] as? Number ?: return null
    val longitude = this["longitude"] as? Number ?: return null
    return LatLng(latitude.toDouble(), longitude.toDouble())
}
