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
    alerts: List<UrbanAlert> = emptyList(),
    currentLocation: LatLng? = null,
    routePoints: List<LatLng> = emptyList(),
    routeLines: List<List<LatLng>> = emptyList(),
    riskZones: List<MapRiskZone> = emptyList(),
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
                            riskZones = riskZones,
                            selectedRouteIndex = selectedRouteIndex,
                            currentLocationIcon = currentLocationIcon,
                            alertIconFactory = alertIconFactory,
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
                        riskZones = riskZones,
                        selectedRouteIndex = selectedRouteIndex,
                        currentLocationIcon = currentLocationIcon,
                        alertIconFactory = alertIconFactory,
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
    riskZones: List<MapRiskZone>,
    selectedRouteIndex: Int,
    currentLocationIcon: org.maplibre.android.annotations.Icon,
    alertIconFactory: IconFactory,
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
                .icon(alertIconFactory.fromBitmap(createAlertIconBitmap(alert)))
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
        "Alta" -> AndroidColor.argb(72, 217, 83, 79)
        "Media" -> AndroidColor.argb(72, 231, 163, 62)
        else -> AndroidColor.argb(62, 60, 111, 98)
    }
}

private fun riskZoneStrokeColor(riskLevel: String): Int {
    return when (riskLevel) {
        "Alta" -> AndroidColor.argb(150, 217, 83, 79)
        "Media" -> AndroidColor.argb(150, 231, 163, 62)
        else -> AndroidColor.argb(140, 60, 111, 98)
    }
}

private fun createAlertIconBitmap(alert: UrbanAlert): Bitmap {
    val size = 76
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val category = "${alert.category} ${alert.title}".lowercase()
    val color = when (alert.urgency) {
        "Alta" -> AndroidColor.rgb(217, 83, 79)
        "Media" -> AndroidColor.rgb(231, 163, 62)
        else -> AndroidColor.rgb(60, 111, 98)
    }

    paint.color = AndroidColor.WHITE
    canvas.drawCircle(size / 2f, size / 2f, 31f, paint)

    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, 26f, paint)

    paint.color = AndroidColor.WHITE
    paint.strokeWidth = 5f
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeJoin = Paint.Join.ROUND
    paint.style = Paint.Style.STROKE

    when {
        category.contains("bache") || category.contains("vía") || category.contains("via") || category.contains("publica") || category.contains("pública") -> {
            drawConstructionIcon(canvas, paint, size)
        }

        category.contains("luminaria") || category.contains("ilumin") -> {
            drawLightbulbIcon(canvas, paint, size)
        }

        category.contains("residuo") || category.contains("basura") -> {
            drawTrashIcon(canvas, paint, size)
        }

        category.contains("tránsito") || category.contains("transito") || category.contains("choque") || category.contains("veh") -> {
            drawCarIcon(canvas, paint, size)
        }

        category.contains("incendio") || category.contains("fuego") -> {
            drawFlameIcon(canvas, paint, size)
        }

        category.contains("bloque") || category.contains("calle") -> {
            drawBlockedIcon(canvas, paint, size)
        }

        else -> {
            drawWarningIcon(canvas, paint, size)
        }
    }

    return bitmap
}

private fun drawConstructionIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    canvas.drawLine(c - 15f, c + 14f, c + 15f, c - 16f, paint)
    canvas.drawLine(c - 13f, c - 14f, c + 16f, c + 15f, paint)
    canvas.drawLine(c + 7f, c - 18f, c + 18f, c - 18f, paint)
    canvas.drawLine(c + 18f, c - 18f, c + 18f, c - 7f, paint)
    canvas.drawLine(c - 18f, c - 8f, c - 8f, c - 18f, paint)
}

private fun drawLightbulbIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    canvas.drawCircle(c, c - 7f, 13f, paint)
    canvas.drawLine(c - 8f, c + 7f, c + 8f, c + 7f, paint)
    canvas.drawLine(c - 6f, c + 15f, c + 6f, c + 15f, paint)
    canvas.drawLine(c - 4f, c + 22f, c + 4f, c + 22f, paint)
}

private fun drawTrashIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    canvas.drawLine(c - 15f, c - 16f, c + 15f, c - 16f, paint)
    canvas.drawLine(c - 7f, c - 23f, c + 7f, c - 23f, paint)
    canvas.drawRoundRect(c - 13f, c - 10f, c + 13f, c + 22f, 4f, 4f, paint)
    canvas.drawLine(c - 5f, c - 3f, c - 5f, c + 15f, paint)
    canvas.drawLine(c + 5f, c - 3f, c + 5f, c + 15f, paint)
}

private fun drawCarIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    canvas.drawRoundRect(c - 20f, c - 3f, c + 20f, c + 17f, 7f, 7f, paint)
    canvas.drawLine(c - 11f, c - 3f, c - 5f, c - 15f, paint)
    canvas.drawLine(c - 5f, c - 15f, c + 9f, c - 15f, paint)
    canvas.drawLine(c + 9f, c - 15f, c + 16f, c - 3f, paint)
    canvas.drawCircle(c - 11f, c + 19f, 3.5f, paint)
    canvas.drawCircle(c + 11f, c + 19f, 3.5f, paint)
}

private fun drawFlameIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    val flame = android.graphics.Path().apply {
        moveTo(c, c + 23f)
        cubicTo(c - 18f, c + 8f, c - 8f, c - 4f, c - 3f, c - 15f)
        cubicTo(c + 1f, c - 6f, c + 13f, c - 3f, c + 7f, c - 25f)
        cubicTo(c + 24f, c - 6f, c + 20f, c + 12f, c, c + 23f)
        close()
    }
    canvas.drawPath(flame, paint)
}

private fun drawBlockedIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    canvas.drawRoundRect(c - 21f, c - 13f, c + 21f, c + 13f, 6f, 6f, paint)
    canvas.drawLine(c - 13f, c, c + 13f, c, paint)
}

private fun drawWarningIcon(canvas: Canvas, paint: Paint, size: Int) {
    val c = size / 2f
    val triangle = android.graphics.Path().apply {
        moveTo(c, c - 22f)
        lineTo(c - 22f, c + 18f)
        lineTo(c + 22f, c + 18f)
        close()
    }
    canvas.drawPath(triangle, paint)
    canvas.drawLine(c, c - 6f, c, c + 6f, paint)
    paint.style = Paint.Style.FILL
    canvas.drawCircle(c, c + 14f, 2.8f, paint)
    paint.style = Paint.Style.STROKE
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
