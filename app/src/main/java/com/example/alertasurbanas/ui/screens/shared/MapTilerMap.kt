package com.example.alertasurbanas.ui.screens.shared

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ZoomControls
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alertasurbanas.BuildConfig
import com.example.alertasurbanas.R
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.MapRiskZone
import com.example.alertasurbanas.data.UrbanAlert
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapTilerMap(
    modifier: Modifier = Modifier,
    center: LatLng = MapDefaults.UtcjLocation,
    zoom: Double = 13.0,
    showNativeControls: Boolean = true,
    alerts: List<UrbanAlert> = emptyList(),
    currentLocation: LatLng? = null,
    routePoints: List<LatLng> = emptyList(),
    routeLines: List<List<LatLng>> = emptyList(),
    riskZones: List<MapRiskZone> = emptyList(),
    selectedRouteIndex: Int = 0,
    onAlertSelected: (UrbanAlert) -> Unit = {},
    onMapClick: (LatLng) -> Unit = {},
    onCameraIdle: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val initialized = remember { booleanArrayOf(false) }
    val cameraIdleListenerAdded = remember { booleanArrayOf(false) }
    val mapClickListenerAdded = remember { booleanArrayOf(false) }
    val lastAppliedCenter = remember { arrayOf<LatLng?>(null) }
    val lastAppliedZoom = remember { doubleArrayOf(Double.NaN) }
    val latestOnMapClick = rememberUpdatedState(onMapClick)
    val currentLocationIcon = remember {
        IconFactory.getInstance(context).fromBitmap(createCurrentLocationBitmap())
    }
    val routeStartIcon = remember {
        IconFactory.getInstance(context).fromBitmap(
            createRouteEndpointBitmap(label = "A", color = AndroidColor.rgb(60, 111, 98))
        )
    }
    val routeEndIcon = remember {
        IconFactory.getInstance(context).fromBitmap(
            createRouteEndpointBitmap(label = "B", color = AndroidColor.rgb(205, 126, 70))
        )
    }
    val alertIconFactory = remember {
        IconFactory.getInstance(context)
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
            if (!showNativeControls) {
                view.post { hideNativeZoomControls(view) }
            }

            view.getMapAsync { map ->
                val styleUrl =
                    "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"

                if (!initialized[0]) {
                    map.uiSettings.isCompassEnabled = showNativeControls
                    map.uiSettings.isAttributionEnabled = showNativeControls
                    map.uiSettings.isLogoEnabled = showNativeControls

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
                            riskZones = riskZones,
                            selectedRouteIndex = selectedRouteIndex,
                            currentLocationIcon = currentLocationIcon,
                            routeStartIcon = routeStartIcon,
                            routeEndIcon = routeEndIcon,
                            alertIconFactory = alertIconFactory,
                            context = context,
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

                        if (!mapClickListenerAdded[0]) {
                            mapClickListenerAdded[0] = true
                            map.addOnMapClickListener { point ->
                                latestOnMapClick.value(point)
                                true
                            }
                        }
                    }
                } else {
                    map.uiSettings.isCompassEnabled = showNativeControls
                    map.uiSettings.isAttributionEnabled = showNativeControls
                    map.uiSettings.isLogoEnabled = showNativeControls

                    syncMap(
                        map = map,
                        center = center,
                        zoom = zoom,
                        alerts = alerts,
                        currentLocation = currentLocation,
                        routePoints = routePoints,
                        routeLines = routeLines,
                        riskZones = riskZones,
                        selectedRouteIndex = selectedRouteIndex,
                        currentLocationIcon = currentLocationIcon,
                        routeStartIcon = routeStartIcon,
                        routeEndIcon = routeEndIcon,
                        alertIconFactory = alertIconFactory,
                        context = context,
                        lastAppliedCenter = lastAppliedCenter,
                        lastAppliedZoom = lastAppliedZoom,
                        onAlertSelected = onAlertSelected
                    )
                }
            }
        }
    )
}

private fun hideNativeZoomControls(view: View) {
    if (view is ZoomControls) {
        view.visibility = View.GONE
        return
    }

    if (view is ViewGroup) {
        for (index in 0 until view.childCount) {
            hideNativeZoomControls(view.getChildAt(index))
        }
    }
}

private fun syncMap(
    map: MapLibreMap,
    center: LatLng,
    zoom: Double,
    alerts: List<UrbanAlert>,
    currentLocation: LatLng?,
    routePoints: List<LatLng>,
    routeLines: List<List<LatLng>>,
    riskZones: List<MapRiskZone>,
    selectedRouteIndex: Int,
    currentLocationIcon: org.maplibre.android.annotations.Icon,
    routeStartIcon: org.maplibre.android.annotations.Icon,
    routeEndIcon: org.maplibre.android.annotations.Icon,
    alertIconFactory: IconFactory,
    context: Context,
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

    riskZones.forEach { zone ->
        map.addPolygon(
            PolygonOptions()
                .addAll(createCirclePoints(zone))
                .fillColor(riskZoneFillColor(zone.riskLevel))
                .strokeColor(riskZoneStrokeColor(zone.riskLevel))
        )
    }

    val routesToDraw = routeLines.ifEmpty {
        if (routePoints.size >= 2) listOf(routePoints) else emptyList()
    }

    val orderedRoutes = routesToDraw
        .mapIndexed { index, route -> index to route }
        .sortedBy { (index, _) -> if (index == selectedRouteIndex) 1 else 0 }

    orderedRoutes.forEach { (index, route) ->
        if (route.size < 2) return@forEach

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

    routesToDraw.getOrNull(selectedRouteIndex)?.takeIf { it.size >= 2 }?.let { selectedRoute ->
        map.addMarker(
            MarkerOptions()
                .position(selectedRoute.first())
                .icon(routeStartIcon)
        )

        map.addMarker(
            MarkerOptions()
                .position(selectedRoute.last())
                .icon(routeEndIcon)
        )
    }

    currentLocation?.let { location ->
        map.addMarker(
            MarkerOptions()
                .position(location)
                .icon(currentLocationIcon)
        )
    }

    alerts.forEach { alert ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(alert.latitude, alert.longitude))
                .icon(alertIconFactory.fromBitmap(createAlertIconBitmap(context, alert)))
        )
    }

    map.setOnMarkerClickListener { marker ->
        val selected = alerts.firstOrNull { alert ->
            abs(alert.latitude - marker.position.latitude) < 0.000001 &&
                abs(alert.longitude - marker.position.longitude) < 0.000001
        }
        if (selected != null) {
            onAlertSelected(selected)
            true
        } else {
            true
        }
    }
}

private fun createCirclePoints(zone: MapRiskZone, steps: Int = 64): List<LatLng> {
    val earthRadius = 6_371_000.0
    val latRadians = Math.toRadians(zone.latitude)
    val angularDistance = zone.radiusMeters / earthRadius

    return (0..steps).map { index ->
        val bearing = 2.0 * Math.PI * index / steps
        val latitude = Math.asin(
            sin(latRadians) * cos(angularDistance) +
                cos(latRadians) * sin(angularDistance) * cos(bearing)
        )
        val longitude = Math.toRadians(zone.longitude) + Math.atan2(
            sin(bearing) * sin(angularDistance) * cos(latRadians),
            cos(angularDistance) - sin(latRadians) * sin(latitude)
        )

        LatLng(Math.toDegrees(latitude), Math.toDegrees(longitude))
    }
}

private fun riskZoneFillColor(riskLevel: String): Int {
    return when (riskLevel) {
        "Alta" -> AndroidColor.argb(112, 217, 83, 79)
        "Media" -> AndroidColor.argb(104, 231, 163, 62)
        else -> AndroidColor.argb(92, 60, 111, 98)
    }
}

private fun riskZoneStrokeColor(riskLevel: String): Int {
    return when (riskLevel) {
        "Alta" -> AndroidColor.argb(210, 217, 83, 79)
        "Media" -> AndroidColor.argb(205, 231, 163, 62)
        else -> AndroidColor.argb(195, 60, 111, 98)
    }
}

private fun createAlertIconBitmap(context: Context, alert: UrbanAlert): Bitmap {
    val size = 84
    val bitmap = Bitmap.createBitmap(size, 96, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val category = "${alert.category} ${alert.title}".lowercase()
    val color = when (alert.urgency) {
        "Alta" -> AndroidColor.rgb(217, 83, 79)
        "Media" -> AndroidColor.rgb(231, 163, 62)
        else -> AndroidColor.rgb(60, 111, 98)
    }

    val centerX = size / 2f
    val centerY = 36f

    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.argb(55, 0, 0, 0)
    canvas.drawCircle(centerX, centerY + 4f, 32f, paint)

    paint.color = AndroidColor.WHITE
    canvas.drawCircle(centerX, centerY, 33f, paint)

    paint.color = color
    canvas.drawCircle(centerX, centerY, 28f, paint)

    val pinTip = android.graphics.Path().apply {
        moveTo(centerX - 13f, centerY + 24f)
        lineTo(centerX + 13f, centerY + 24f)
        lineTo(centerX, 82f)
        close()
    }
    canvas.drawPath(pinTip, paint)

    drawVectorIcon(
        context = context,
        canvas = canvas,
        resId = iconResourceForCategory(category),
        left = 25,
        top = 19,
        right = 59,
        bottom = 53
    )

    return bitmap
}

private fun iconResourceForCategory(category: String): Int {
    return when {
        category.contains("bloque") || category.contains("calle cerrada") || category.contains("obstru") -> R.drawable.ic_alert_block
        category.contains("bache") || category.contains("vía") || category.contains("via") || category.contains("publica") || category.contains("pública") -> R.drawable.ic_alert_construction
        category.contains("luminaria") || category.contains("ilumin") -> R.drawable.ic_alert_lightbulb
        category.contains("residuo") || category.contains("basura") -> R.drawable.ic_alert_delete
        category.contains("tránsito") || category.contains("transito") || category.contains("choque") || category.contains("veh") -> R.drawable.ic_alert_car
        category.contains("incendio") || category.contains("fuego") -> R.drawable.ic_alert_fire
        category.contains("seguridad") || category.contains("riesgo") -> R.drawable.ic_alert_shield
        else -> R.drawable.ic_alert_warning
    }
}

private fun drawVectorIcon(
    context: Context,
    canvas: Canvas,
    resId: Int,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
) {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return
    drawable.setBounds(left, top, right, bottom)
    drawable.draw(canvas)
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

private fun createRouteEndpointBitmap(label: String, color: Int): Bitmap {
    val size = 72
    val bitmap = Bitmap.createBitmap(size, 86, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val centerX = size / 2f
    val centerY = 31f

    paint.style = Paint.Style.FILL
    paint.color = AndroidColor.argb(45, 0, 0, 0)
    canvas.drawCircle(centerX, centerY + 4f, 27f, paint)

    paint.color = AndroidColor.WHITE
    canvas.drawCircle(centerX, centerY, 28f, paint)

    paint.color = color
    canvas.drawCircle(centerX, centerY, 23f, paint)

    val pinTip = android.graphics.Path().apply {
        moveTo(centerX - 10f, centerY + 20f)
        lineTo(centerX + 10f, centerY + 20f)
        lineTo(centerX, 76f)
        close()
    }
    canvas.drawPath(pinTip, paint)

    paint.color = AndroidColor.WHITE
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 26f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    val textY = centerY - ((paint.descent() + paint.ascent()) / 2)
    canvas.drawText(label, centerX, textY, paint)

    return bitmap
}
