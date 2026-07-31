package com.example.alertasurbanas.ui.screens.shared

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alertasurbanas.BuildConfig
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.UrbanAlert
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.abs

@Composable
fun MapTilerMap(
    modifier: Modifier = Modifier,
    center: LatLng = MapDefaults.UtcjLocation,
    zoom: Double = 13.0,
    alerts: List<UrbanAlert> = emptyList(),
    currentLocation: LatLng? = null,
    routePoints: List<LatLng> = emptyList(),
    routeLines: List<List<LatLng>> = emptyList(),
    selectedRouteIndex: Int = 0,
    onAlertSelected: (UrbanAlert) -> Unit = {},
    onCameraIdle: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val initialized = remember { booleanArrayOf(false) }
    val cameraIdleListenerAdded = remember { booleanArrayOf(false) }
    val lastAppliedCenter = remember { arrayOf<LatLng?>(null) }
    val lastAppliedZoom = remember { doubleArrayOf(Double.NaN) }
    val currentLocationIcon = remember {
        IconFactory.getInstance(context).fromBitmap(createCurrentLocationBitmap())
    }

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
                            currentLocation = currentLocation,
                            routePoints = routePoints,
                            routeLines = routeLines,
                            selectedRouteIndex = selectedRouteIndex,
                            currentLocationIcon = currentLocationIcon,
                            lastAppliedCenter = lastAppliedCenter,
                            lastAppliedZoom = lastAppliedZoom,
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
                        currentLocation = currentLocation,
                        routePoints = routePoints,
                        routeLines = routeLines,
                        selectedRouteIndex = selectedRouteIndex,
                        currentLocationIcon = currentLocationIcon,
                        lastAppliedCenter = lastAppliedCenter,
                        lastAppliedZoom = lastAppliedZoom,
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
    currentLocation: LatLng?,
    routePoints: List<LatLng>,
    routeLines: List<List<LatLng>>,
    selectedRouteIndex: Int,
    currentLocationIcon: org.maplibre.android.annotations.Icon,
    lastAppliedCenter: Array<LatLng?>,
    lastAppliedZoom: DoubleArray,
    onAlertSelected: (UrbanAlert) -> Unit
) {
    val previousCenter = lastAppliedCenter[0]
    val shouldMoveCamera = previousCenter == null ||
        abs(previousCenter.latitude - center.latitude) > 0.00001 ||
        abs(previousCenter.longitude - center.longitude) > 0.00001 ||
        abs(lastAppliedZoom[0] - zoom) > 0.01

    if (shouldMoveCamera) {
        map.cameraPosition = CameraPosition.Builder()
            .target(center)
            .zoom(zoom)
            .build()
        lastAppliedCenter[0] = center
        lastAppliedZoom[0] = zoom
    }

    map.clear()

    val routesToDraw = routeLines.ifEmpty {
        if (routePoints.size >= 2) listOf(routePoints) else emptyList()
    }

    routesToDraw.forEachIndexed { index, route ->
        if (route.size < 2) return@forEachIndexed

        val isSelected = index == selectedRouteIndex
        map.addPolyline(
            PolylineOptions()
                .addAll(route)
                .color(
                    if (isSelected) {
                        AndroidColor.rgb(60, 111, 98)
                    } else {
                        AndroidColor.argb(165, 205, 126, 70)
                    }
                )
                .width(if (isSelected) 8f else 5f)
        )
    }

    currentLocation?.let { location ->
        map.addMarker(
            MarkerOptions()
                .position(location)
                .title("Tu ubicación")
                .icon(currentLocationIcon)
        )
    }

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
            true
        }
    }
}

private fun createCurrentLocationBitmap(): Bitmap {
    val size = 54
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = AndroidColor.argb(60, 66, 133, 244)
    canvas.drawCircle(size / 2f, size / 2f, 25f, paint)

    paint.color = AndroidColor.WHITE
    canvas.drawCircle(size / 2f, size / 2f, 13f, paint)

    paint.color = AndroidColor.rgb(66, 133, 244)
    canvas.drawCircle(size / 2f, size / 2f, 9f, paint)

    return bitmap
}
