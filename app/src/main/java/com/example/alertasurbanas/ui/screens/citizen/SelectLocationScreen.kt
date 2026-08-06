package com.example.alertasurbanas.ui.screens.citizen

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alertasurbanas.data.AlertRepository
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.MapRouteResult
import com.example.alertasurbanas.data.MapRouteService
import com.example.alertasurbanas.data.MapSearchResult
import com.example.alertasurbanas.data.MapSearchService
import com.example.alertasurbanas.data.UrbanAlert
import com.example.alertasurbanas.ui.screens.shared.MapTilerMap
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import com.example.alertasurbanas.ui.theme.UrbanColors
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val LocationPrimary = UrbanColors.Primary
private val LocationAccent = UrbanColors.Terracotta
private val LocationText = UrbanColors.TextPrimary

@Composable
fun SelectLocationScreen(
    initialAddress: String = "Av. Independencia 250, Col. Centro",
    nearbyAlerts: List<UrbanAlert> = emptyList(),
    onBack: () -> Unit = {},
    onConfirm: (String, LatLng) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var isSearchActive by remember { mutableStateOf(false) }
    var mapCenter by remember { mutableStateOf(MapDefaults.UtcjLocation) }
    var selectedCenter by remember { mutableStateOf(mapCenter) }
    var selectedAddress by remember { mutableStateOf(initialAddress) }
    var mapZoom by remember { mutableStateOf(16.0) }
    var currentLocation by remember { mutableStateOf<LatLng?>(MapDefaults.UtcjLocation) }
    var selectedPlace by remember { mutableStateOf<MapSearchResult?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeResult by remember { mutableStateOf<MapRouteResult?>(null) }
    var routeResults by remember { mutableStateOf<List<MapRouteResult>>(emptyList()) }
    var selectedRouteIndex by remember { mutableStateOf(0) }
    var isRouteLoading by remember { mutableStateOf(false) }

    fun moveToResult(result: MapSearchResult) {
        searchQuery = result.title
        suggestions = emptyList()
        isSearchActive = false
        selectedCenter = result.coordinate
        mapCenter = result.coordinate
        mapZoom = 17.0
        selectedAddress = result.subtitle.substringBefore(" · ")
        selectedPlace = result
        routePoints = emptyList()
        routeResult = null
        routeResults = emptyList()
        selectedRouteIndex = 0
    }

    fun searchPlace() {
        if (searchQuery.isBlank()) {
            Toast.makeText(context, "Escribe una dirección o lugar.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val result = MapSearchService.searchResults(
                query = searchQuery,
                limit = 1,
                proximity = selectedCenter
            ).firstOrNull()

            if (result != null) {
                moveToResult(result)
            } else {
                Toast.makeText(context, "No encontré esa dirección cerca.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun centerOnDeviceLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val coordinate = LatLng(location.latitude, location.longitude)
                        currentLocation = coordinate
                        selectedCenter = coordinate
                        mapCenter = coordinate
                        mapZoom = 17.0
                        selectedPlace = null
                        routePoints = emptyList()
                        routeResult = null
                        routeResults = emptyList()
                        selectedRouteIndex = 0
                    } else {
                        Toast.makeText(
                            context,
                            "En emulador asigna una ubicación simulada.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (_: SecurityException) {
            Toast.makeText(context, "Activa el permiso de ubicación.", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) centerOnDeviceLocation()
    }

    fun requestCurrentLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            centerOnDeviceLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun calculateRoute(destination: LatLng) {
        val origin = currentLocation ?: MapDefaults.UtcjLocation

        scope.launch {
            isRouteLoading = true
            val results = MapRouteService.drivingRoutes(origin, destination, targetCount = 3)
                .map { route ->
                    route.withNearbyReports(countReportsNearRoute(route.points, nearbyAlerts.ifEmpty { AlertRepository.publicAlerts }))
                }
                .sortedWith(compareBy<MapRouteResult> { it.nearbyReports }.thenBy { it.durationMinutes })
            isRouteLoading = false

            if (results.isNotEmpty()) {
                routeResults = results
                selectedRouteIndex = 0
                routeResult = results.first()
                routePoints = results.first().points
                mapCenter = destination
                mapZoom = 14.5
                Toast.makeText(context, "Se encontraron ${results.size} ruta(s).", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "No se pudo calcular la ruta. Revisa tu ORS_API_KEY o internet.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            centerOnDeviceLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(searchQuery, isSearchActive, currentLocation) {
        if (!isSearchActive || searchQuery.length < 2) {
            suggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        suggestions = MapSearchService.searchResults(
            query = searchQuery,
            limit = 5,
            proximity = currentLocation ?: MapDefaults.UtcjLocation,
            radiusMeters = 0
        )
    }

    LaunchedEffect(selectedCenter) {
        delay(450)
        val address = MapSearchService.reverseGeocode(selectedCenter)
        if (!address.isNullOrBlank()) selectedAddress = address
    }

    Scaffold(
        containerColor = UrbanColors.MapBackground,
        bottomBar = {
            LocationConfirmation(
                address = selectedAddress,
                selectedPlace = selectedPlace,
                routeResult = routeResult,
                routeResults = routeResults,
                selectedRouteIndex = selectedRouteIndex,
                isRouteLoading = isRouteLoading,
                onRouteSelected = { index ->
                    selectedRouteIndex = index
                    routeResult = routeResults.getOrNull(index)
                    routePoints = routeResults.getOrNull(index)?.points.orEmpty()
                },
                onRouteClick = {
                    calculateRoute(selectedPlace?.coordinate ?: selectedCenter)
                },
                onConfirm = { onConfirm(selectedAddress, selectedCenter) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MapTilerMap(
                modifier = Modifier.fillMaxSize(),
                center = mapCenter,
                zoom = mapZoom,
                currentLocation = currentLocation,
                routePoints = routePoints,
                routeLines = routeResults.map { it.points },
                selectedRouteIndex = selectedRouteIndex,
                onCameraIdle = { coordinate ->
                    selectedCenter = coordinate
                    selectedPlace = null
                    routePoints = emptyList()
                    routeResult = null
                    routeResults = emptyList()
                    selectedRouteIndex = 0
                }
            )

            LocationSearchHeader(
                searchQuery = searchQuery,
                isSearchActive = isSearchActive,
                onSearchQueryChange = {
                    searchQuery = it
                    isSearchActive = true
                },
                suggestions = suggestions,
                onBack = onBack,
                onSearch = { searchPlace() },
                onClearSearch = {
                    searchQuery = ""
                    suggestions = emptyList()
                    isSearchActive = false
                },
                onSuggestionSelected = { moveToResult(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            if (!isSearchActive) {
                MapControls(
                    onMyLocation = { requestCurrentLocation() },
                    onZoomIn = { mapZoom = (mapZoom + 1.0).coerceAtMost(19.0) },
                    onZoomOut = { mapZoom = (mapZoom - 1.0).coerceAtLeast(4.0) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 128.dp, end = 16.dp)
                )
            }

            CenterLocationMarker(modifier = Modifier.align(Alignment.Center))

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 88.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                shadowElevation = 3.dp
            ) {
                Text(
                    text = "Mueve el mapa para ajustar",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    color = LocationText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun LocationSearchHeader(
    searchQuery: String,
    isSearchActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<MapSearchResult>,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onSuggestionSelected: (MapSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(onClick = onBack, shape = CircleShape, color = Color.White, shadowElevation = 5.dp) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = LocationText,
                modifier = Modifier.padding(13.dp)
            )
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.97f),
            shadowElevation = 5.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SearchInputBox(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    onSearch = onSearch,
                    onClear = onClearSearch
                )

        if (isSearchActive && searchQuery.length >= 2) {
            Spacer(modifier = Modifier.height(8.dp))
                    if (suggestions.isEmpty()) {
                        Text(
                            text = "Buscando direcciones cercanas...",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = LocationText.copy(alpha = 0.58f),
                            fontSize = 12.sp
                        )
                    } else {
                        suggestions.forEach { suggestion ->
                            SearchSuggestionRow(
                                suggestion = suggestion,
                                onClick = { onSuggestionSelected(suggestion) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.height(50.dp),
        shape = RoundedCornerShape(15.dp),
        color = Color.White,
        border = BorderStroke(1.4.dp, LocationPrimary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = LocationText.copy(alpha = 0.55f),
                modifier = Modifier.size(21.dp)
            )

            Spacer(modifier = Modifier.width(9.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isBlank()) {
                    Text(
                        text = "Buscar dirección",
                        color = LocationText.copy(alpha = 0.45f),
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = LocationText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }

            IconButton(onClick = if (value.isBlank()) onSearch else onClear) {
                Icon(
                    imageVector = if (value.isBlank()) Icons.Outlined.Search else Icons.Outlined.Close,
                    contentDescription = null,
                    tint = LocationPrimary,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchSuggestionRow(
    suggestion: MapSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = LocationPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.title,
                color = LocationText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = suggestion.subtitle,
                color = LocationText.copy(alpha = 0.56f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MapControls(
    onMyLocation: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(onClick = onMyLocation, shape = CircleShape, color = Color.White, shadowElevation = 5.dp) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = "Mi ubicación",
                tint = LocationText,
                modifier = Modifier.padding(13.dp)
            )
        }

        Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 5.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onZoomIn) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = "Acercar", tint = LocationText)
                }
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(UrbanColors.Border)
                )
                IconButton(onClick = onZoomOut) {
                    Icon(imageVector = Icons.Outlined.Remove, contentDescription = "Alejar", tint = LocationText)
                }
            }
        }
    }
}

@Composable
private fun CenterLocationMarker(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(bottom = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = LocationAccent, shadowElevation = 7.dp) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "Ubicación seleccionada",
                tint = Color.White,
                modifier = Modifier.padding(13.dp).size(30.dp)
            )
        }
        Box(modifier = Modifier.width(3.dp).height(17.dp).background(LocationAccent))
        Box(
            modifier = Modifier
                .size(width = 30.dp, height = 7.dp)
                .background(color = Color.Black.copy(alpha = 0.15f), shape = CircleShape)
        )
    }
}

@Composable
private fun LocationConfirmation(
    address: String,
    selectedPlace: MapSearchResult?,
    routeResult: MapRouteResult?,
    routeResults: List<MapRouteResult>,
    selectedRouteIndex: Int,
    isRouteLoading: Boolean,
    onRouteSelected: (Int) -> Unit,
    onRouteClick: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .background(color = UrbanColors.Handle, shape = CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(17.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = LocationPrimary.copy(alpha = 0.10f)) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = LocationPrimary,
                        modifier = Modifier.padding(11.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ubicación seleccionada",
                        color = LocationText.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = address,
                        color = LocationText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            selectedPlace?.let { place ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    color = LocationPrimary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, LocationPrimary.copy(alpha = 0.18f))
                ) {
                    Column(modifier = Modifier.padding(13.dp)) {
                        Text(
                            text = place.title,
                            color = LocationText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = place.subtitle,
                            color = LocationText.copy(alpha = 0.62f),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        routeResult?.let { route ->
                            Text(
                                text = "Ruta: ${route.summaryText}",
                                color = LocationPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (routeResults.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        routeResults.forEachIndexed { index, route ->
                            LocationRouteOptionRow(
                                routeNumber = index + 1,
                                route = route,
                                selected = index == selectedRouteIndex,
                                onClick = { onRouteSelected(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = onRouteClick,
                    enabled = !isRouteLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocationAccent)
                ) {
                    Icon(imageVector = Icons.Outlined.Route, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRouteLoading) "Calculando ruta..." else "Ver ruta hacia aquí",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocationPrimary)
            ) {
                Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Confirmar ubicación", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LocationRouteOptionRow(
    routeNumber: Int,
    route: MapRouteResult,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = if (selected) LocationPrimary.copy(alpha = 0.10f) else UrbanColors.NeutralPanel,
        border = BorderStroke(1.dp, if (selected) LocationPrimary else UrbanColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) LocationPrimary else LocationText.copy(alpha = 0.12f)
            ) {
                Text(
                    text = routeNumber.toString(),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    color = if (selected) Color.White else LocationText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.summaryText,
                    color = LocationText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "${route.nearbyReports} reporte(s) cerca",
                    color = if (route.nearbyReports == 0) LocationPrimary else UrbanColors.HighUrgency,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Ruta seleccionada",
                    tint = LocationPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun countReportsNearRoute(
    routePoints: List<LatLng>,
    alerts: List<UrbanAlert>,
    thresholdMeters: Double = 250.0
): Int {
    if (routePoints.size < 2) return 0

    return alerts.count { alert ->
        val alertPoint = LatLng(alert.latitude, alert.longitude)
        routePoints.windowed(2).any { segment ->
            distancePointToSegmentMeters(
                point = alertPoint,
                segmentStart = segment[0],
                segmentEnd = segment[1]
            ) <= thresholdMeters
        }
    }
}

private fun distancePointToSegmentMeters(
    point: LatLng,
    segmentStart: LatLng,
    segmentEnd: LatLng
): Double {
    val metersPerDegreeLat = 111_320.0
    val metersPerDegreeLon = 111_320.0 * cos(Math.toRadians(point.latitude))

    val px = point.longitude * metersPerDegreeLon
    val py = point.latitude * metersPerDegreeLat
    val ax = segmentStart.longitude * metersPerDegreeLon
    val ay = segmentStart.latitude * metersPerDegreeLat
    val bx = segmentEnd.longitude * metersPerDegreeLon
    val by = segmentEnd.latitude * metersPerDegreeLat

    val dx = bx - ax
    val dy = by - ay

    if (dx == 0.0 && dy == 0.0) {
        return haversineMeters(point, segmentStart)
    }

    val t = (((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)).coerceIn(0.0, 1.0)
    val closestX = ax + t * dx
    val closestY = ay + t * dy

    return sqrt((px - closestX).pow(2.0) + (py - closestY).pow(2.0))
}

private fun haversineMeters(from: LatLng, to: LatLng): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(to.latitude - from.latitude)
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val fromLat = Math.toRadians(from.latitude)
    val toLat = Math.toRadians(to.latitude)

    val a = sin(dLat / 2).pow(2.0) + cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadiusMeters * c
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SelectLocationPreview() {
    AlertasUrbanasTheme {
        SelectLocationScreen()
    }
}
