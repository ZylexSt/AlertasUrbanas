package com.example.alertasurbanas.ui.screens.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alertasurbanas.BuildConfig
import com.example.alertasurbanas.data.UrbanAlert
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.abs

@Composable
fun MapTilerMap(
    modifier: Modifier = Modifier,
    center: LatLng = LatLng(31.761, -106.485),
    zoom: Double = 13.0,
    alerts: List<UrbanAlert> = emptyList(),
    onAlertSelected: (UrbanAlert) -> Unit = {},
    onCameraIdle: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val initialized = remember { booleanArrayOf(false) }
    val cameraIdleListenerAdded = remember { booleanArrayOf(false) }
    val lastAppliedCenter = remember { arrayOf<LatLng?>(null) }

    val mapView = remember {
        MapLibre.getInstance(context)

        MapView(context).apply {
            onCreate(null)
            onStart()
            onResume()
        }
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.getMapAsync { map ->
                val styleUrl =
                    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

                if (!initialized[0]) {
                    map.setStyle(styleUrl) {
                        initialized[0] = true
                        syncMap(
                            map = map,
                            center = center,
                            zoom = zoom,
                            alerts = alerts,
                            lastAppliedCenter = lastAppliedCenter,
                            onAlertSelected = onAlertSelected
                        )

                        if (!cameraIdleListenerAdded[0]) {
                            cameraIdleListenerAdded[0] = true
                            map.addOnCameraIdleListener {
                                map.cameraPosition.target?.let(onCameraIdle)
                            }
                        }
                    }
                } else {
                    syncMap(
                        map = map,
                        center = center,
                        zoom = zoom,
                        alerts = alerts,
                        lastAppliedCenter = lastAppliedCenter,
                        onAlertSelected = onAlertSelected
                    )
                }
            }
        }
    )
}

private fun syncMap(
    map: MapLibreMap,
    center: LatLng,
    zoom: Double,
    alerts: List<UrbanAlert>,
    lastAppliedCenter: Array<LatLng?>,
    onAlertSelected: (UrbanAlert) -> Unit
) {
    val previousCenter = lastAppliedCenter[0]
    val shouldMoveCamera = previousCenter == null ||
        abs(previousCenter.latitude - center.latitude) > 0.00001 ||
        abs(previousCenter.longitude - center.longitude) > 0.00001

    if (shouldMoveCamera) {
        map.cameraPosition = CameraPosition.Builder()
            .target(center)
            .zoom(zoom)
            .build()
        lastAppliedCenter[0] = center
    }

    map.clear()

    alerts.forEach { alert ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(alert.latitude, alert.longitude))
                .title(alert.id)
                .snippet("${alert.title} · ${alert.urgency}")
        )
    }

    map.setOnMarkerClickListener { marker ->
        val selected = alerts.firstOrNull { it.id == marker.title }
        if (selected != null) {
            onAlertSelected(selected)
            true
        } else {
            false
        }
    }
}
