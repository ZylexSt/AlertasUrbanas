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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.alertasurbanas.data.CarMapSyncRepository
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.MapRouteResult
import com.example.alertasurbanas.data.MapRouteService
import com.example.alertasurbanas.data.MapSearchResult
import com.example.alertasurbanas.data.MapSearchService
import com.example.alertasurbanas.data.UrbanAlert
import com.example.alertasurbanas.ui.screens.shared.rememberLiveUserLocation
import com.example.alertasurbanas.ui.screens.shared.MapTilerMap
import com.example.alertasurbanas.ui.screens.shared.UrbanBottomBar
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

private val MapBackground = UrbanColors.MapBackground
private val MapPrimary = UrbanColors.Primary
private val MapText = UrbanColors.TextPrimary
private val MapHigh = UrbanColors.HighUrgency
private val MapMedium = UrbanColors.MediumUrgency
private val MapLow = UrbanColors.Primary

private object ActiveRouteSession {
    var routePoints: List<LatLng> = emptyList()
    var routeResult: MapRouteResult? = null
    var routeResults: List<MapRouteResult> = emptyList()
    var selectedRouteIndex: Int = 0
    var originQuery: String = MapDefaults.UtcjAddress
    var originCoordinate: LatLng? = MapDefaults.UtcjLocation
    var destinationCoordinate: LatLng? = null
    var destinationName: String = ""
    var started: Boolean = false

    fun clear() {
        routePoints = emptyList()
        routeResult = null
        routeResults = emptyList()
        selectedRouteIndex = 0
        originQuery = MapDefaults.UtcjAddress
        originCoordinate = MapDefaults.UtcjLocation
        destinationCoordinate = null
        destinationName = ""
        started = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    alerts: List<UrbanAlert> = emptyList(),
    focusAlert: UrbanAlert? = null,
    centerOnUserLocationOnOpen: Boolean = false,
    onUserLocationCenterHandled: () -> Unit = {},
    onFocusHandled: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onOpenAlert: (UrbanAlert) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val liveUserLocation by rememberLiveUserLocation(
        autoRequestPermission = true,
        intervalMillis = 4_000L,
        minUpdateDistanceMeters = 4f
    )
    val carMapSyncRepository = remember { CarMapSyncRepository() }
    val syncedMapState by carMapSyncRepository.observeRoute().collectAsState(
        initial = com.example.alertasurbanas.data.CarSyncedRoute()
    )

    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedUrgency by remember { mutableStateOf("Todas") }
    var selectedAlert by remember { mutableStateOf<UrbanAlert?>(null) }
    var selectedPlace by remember { mutableStateOf<MapSearchResult?>(null) }
    var showSelectedPlaceCard by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var mapCenter by remember { mutableStateOf(MapDefaults.UtcjLocation) }
    var mapZoom by remember { mutableStateOf(13.0) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasCenteredOnLiveLocation by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf(ActiveRouteSession.routePoints) }
    var routeResult by remember { mutableStateOf(ActiveRouteSession.routeResult) }
    var routeResults by remember { mutableStateOf(ActiveRouteSession.routeResults) }
    var selectedRouteIndex by remember { mutableStateOf(ActiveRouteSession.selectedRouteIndex) }
    var isRouteLoading by remember { mutableStateOf(false) }
    var routeOriginQuery by remember { mutableStateOf(ActiveRouteSession.originQuery) }
    var routeOriginSuggestions by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var isRouteOriginSearchActive by remember { mutableStateOf(false) }
    var routeOriginCoordinate by remember { mutableStateOf<LatLng?>(ActiveRouteSession.originCoordinate) }
    var routeDestinationCoordinate by remember { mutableStateOf<LatLng?>(ActiveRouteSession.destinationCoordinate) }
    var routeDestinationName by remember { mutableStateOf(ActiveRouteSession.destinationName) }
    var routeStarted by remember { mutableStateOf(ActiveRouteSession.started) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val distanceOrigin = currentLocation ?: mapCenter
    val baseAlerts = if (alerts.isNotEmpty()) alerts else AlertRepository.publicAlerts
    val alertsWithFocus = focusAlert?.let { target ->
        if (baseAlerts.any { it.id == target.id }) {
            baseAlerts
        } else {
            baseAlerts + target
        }
    } ?: baseAlerts

    val allAlerts = alertsWithFocus
        .map { alert ->
            alert.copy(
                distanceText = formatDistance(
                    haversineMeters(distanceOrigin, LatLng(alert.latitude, alert.longitude))
                )
            )
        }
        .sortedBy { alert ->
            haversineMeters(distanceOrigin, LatLng(alert.latitude, alert.longitude))
        }
    val filteredAlerts = allAlerts.filter { alert ->
        matchesCategoryFilter(alert, selectedCategory) &&
            (selectedUrgency == "Todas" || alert.urgency == selectedUrgency)
    }

    fun saveRouteSession() {
        ActiveRouteSession.routePoints = routePoints
        ActiveRouteSession.routeResult = routeResult
        ActiveRouteSession.routeResults = routeResults
        ActiveRouteSession.selectedRouteIndex = selectedRouteIndex
        ActiveRouteSession.originQuery = routeOriginQuery
        ActiveRouteSession.originCoordinate = routeOriginCoordinate
        ActiveRouteSession.destinationCoordinate = routeDestinationCoordinate
        ActiveRouteSession.destinationName = routeDestinationName
        ActiveRouteSession.started = routeStarted
    }

    fun clearCarRouteSafely() {
        scope.launch {
            try {
                carMapSyncRepository.clearRoute()
            } catch (_: Exception) {
                // La sincronización con Automotive no debe cerrar el mapa móvil.
            }
        }
    }

    fun closeSelectedPlaceCard() {
        selectedPlace = null
        showSelectedPlaceCard = false
        isRouteOriginSearchActive = false
        routeOriginSuggestions = emptyList()
    }

    fun cancelActiveRoute() {
        routePoints = emptyList()
        routeResult = null
        routeResults = emptyList()
        selectedRouteIndex = 0
        routeDestinationCoordinate = null
        routeDestinationName = ""
        routeStarted = false
        ActiveRouteSession.clear()
        clearCarRouteSafely()
    }

    fun publishFiltersSafely(category: String = selectedCategory, urgency: String = selectedUrgency) {
        scope.launch {
            try {
                carMapSyncRepository.publishFilters(
                    selectedCategory = category,
                    selectedUrgency = urgency,
                    source = "mobile"
                )
            } catch (_: Exception) {
            }
        }
    }

    fun moveToSearchResult(result: MapSearchResult) {
        searchQuery = result.title
        searchSuggestions = emptyList()
        isSearchActive = false
        mapCenter = result.coordinate
        mapZoom = 16.5
        selectedAlert = null
        selectedPlace = result
        showSelectedPlaceCard = true
        routePoints = emptyList()
        routeResult = null
        routeResults = emptyList()
        selectedRouteIndex = 0
        routeDestinationCoordinate = null
        routeDestinationName = ""
        clearCarRouteSafely()
    }

    fun selectPlaceFromMapTap(coordinate: LatLng) {
        scope.launch {
            val result = MapSearchService.nearbyPlaceOrAddress(
                coordinate = coordinate,
                proximity = currentLocation ?: MapDefaults.UtcjLocation
            ) ?: MapSearchResult(
                title = "Punto seleccionado",
                subtitle = "Coordenadas: %.5f, %.5f".format(coordinate.latitude, coordinate.longitude),
                coordinate = coordinate
            )

            searchQuery = ""
            searchSuggestions = emptyList()
            isSearchActive = false
            isRouteOriginSearchActive = false
            routeOriginSuggestions = emptyList()
            mapCenter = result.coordinate
            mapZoom = 16.5
            selectedAlert = null
            selectedPlace = result
            showSelectedPlaceCard = true
            routePoints = emptyList()
            routeResult = null
            routeResults = emptyList()
            selectedRouteIndex = 0
            routeDestinationCoordinate = null
            routeDestinationName = ""
            clearCarRouteSafely()
        }
    }

    fun centerOnDeviceLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val coordinate = LatLng(location.latitude, location.longitude)
                        currentLocation = coordinate
                        routeOriginCoordinate = coordinate
                        routeOriginQuery = "Mi ubicacion actual"
                        mapCenter = coordinate
                        mapZoom = 15.5
                        selectedAlert = null
                        selectedPlace = null
                        showSelectedPlaceCard = false
                        routePoints = emptyList()
                        routeResult = null
                        routeResults = emptyList()
                        selectedRouteIndex = 0
                        routeDestinationCoordinate = null
                        routeDestinationName = ""
                        clearCarRouteSafely()
                    } else {
                        Toast.makeText(context, "En emulador asigna una ubicacion simulada.", Toast.LENGTH_LONG).show()
                    }
                }
        } catch (_: SecurityException) {
            Toast.makeText(context, "Activa el permiso de ubicacion.", Toast.LENGTH_SHORT).show()
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

    LaunchedEffect(liveUserLocation) {
        val coordinate = liveUserLocation ?: return@LaunchedEffect
        currentLocation = coordinate

        if (!hasCenteredOnLiveLocation) {
            mapCenter = coordinate
            mapZoom = 15.5
            hasCenteredOnLiveLocation = true
        }

        if (
            routeOriginQuery.equals("Mi ubicación actual", ignoreCase = true) ||
            routeOriginQuery.equals("Mi ubicacion actual", ignoreCase = true) ||
            (!routeStarted && routeOriginQuery == MapDefaults.UtcjAddress)
        ) {
            routeOriginQuery = "Mi ubicación actual"
            routeOriginCoordinate = coordinate
        }
    }

    LaunchedEffect(centerOnUserLocationOnOpen) {
        if (centerOnUserLocationOnOpen) {
            requestCurrentLocation()
            onUserLocationCenterHandled()
        }
    }

    fun searchPlace() {
        if (searchQuery.isBlank()) {
            Toast.makeText(context, "Escribe una dirección o lugar.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val result = MapSearchService.searchResults(
                    query = searchQuery,
                    limit = 1,
                    proximity = currentLocation ?: MapDefaults.UtcjLocation,
                    radiusMeters = 0,
                    showDistance = currentLocation != null
                ).firstOrNull()
                if (result != null) {
                    moveToSearchResult(result)
                } else {
                    Toast.makeText(context, "No encontré esa dirección cerca.", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                searchSuggestions = emptyList()
                Toast.makeText(
                    context,
                    "No se pudo buscar ahora. Revisa internet o la API key.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun calculateRoute(destination: LatLng) {
        val origin = routeOriginCoordinate ?: currentLocation ?: MapDefaults.UtcjLocation
        val destinationName = selectedPlace?.title
            ?: selectedAlert?.title
            ?: searchQuery.takeIf { it.isNotBlank() }
            ?: "Destino seleccionado"

        scope.launch {
            isRouteLoading = true
            val evaluatedRoutes = MapRouteService.drivingRoutes(origin, destination, targetCount = 3)
                .map { route ->
                    route.withRouteReports(
                        nearbyCount = countReportsNearRoute(route.points, filteredAlerts),
                        blockedCount = countBlockedStreetReportsNearRoute(route.points, filteredAlerts)
                    )
                }
                .sortedBy { it.distanceKm }

            val results = evaluatedRoutes
                .filter { it.blockedReports == 0 }
                .sortedBy { it.distanceKm }

            isRouteLoading = false

            if (results.isNotEmpty()) {
                routeResults = results
                selectedRouteIndex = 0
                routeResult = results.first()
                routePoints = results.first().points
                routeDestinationCoordinate = destination
                routeDestinationName = destinationName
                routeStarted = false
                mapCenter = destination
                mapZoom = 14.5
                Toast.makeText(context, "Se encontraron ${results.size} ruta(s).", Toast.LENGTH_SHORT).show()
            } else if (evaluatedRoutes.isNotEmpty()) {
                routePoints = emptyList()
                routeResult = null
                routeResults = emptyList()
                selectedRouteIndex = 0
                Toast.makeText(
                    context,
                    "No hay una ruta disponible sin pasar por una calle bloqueada.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "No se pudo calcular la ruta. Revisa tu ORS_API_KEY o internet.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(searchQuery, isSearchActive, currentLocation) {
        if (!isSearchActive || searchQuery.length < 2) {
            searchSuggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        searchSuggestions = try {
            MapSearchService.searchResults(
                query = searchQuery,
                limit = 5,
                proximity = currentLocation ?: MapDefaults.UtcjLocation,
                showDistance = currentLocation != null,
                radiusMeters = 0
            )
        } catch (_: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(routeOriginQuery, isRouteOriginSearchActive, currentLocation) {
        if (!isRouteOriginSearchActive || routeOriginQuery.length < 2) {
            routeOriginSuggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        routeOriginSuggestions = try {
            MapSearchService.searchResults(
                query = routeOriginQuery,
                limit = 4,
                proximity = currentLocation ?: MapDefaults.UtcjLocation,
                showDistance = currentLocation != null,
                radiusMeters = 0
            )
        } catch (_: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(filteredAlerts, selectedAlert) {
        if (selectedAlert != null && filteredAlerts.none { it.id == selectedAlert?.id }) {
            selectedAlert = null
        }
    }

    LaunchedEffect(syncedMapState.selectedCategory, syncedMapState.selectedUrgency) {
        val syncedCategory = syncedMapState.selectedCategory.ifBlank { "Todas" }
        val syncedUrgency = syncedMapState.selectedUrgency.ifBlank { "Todas" }

        if (selectedCategory != syncedCategory) {
            selectedCategory = syncedCategory
            selectedAlert = null
        }

        if (selectedUrgency != syncedUrgency) {
            selectedUrgency = syncedUrgency
            selectedAlert = null
        }
    }

    LaunchedEffect(syncedMapState.updatedAt, syncedMapState.routes.size) {
        if (
            syncedMapState.updatedAt > 0L &&
            syncedMapState.routes.isEmpty() &&
            syncedMapState.source == "automotive" &&
            syncedMapState.syncType == "empty_route" &&
            routeStarted
        ) {
            routePoints = emptyList()
            routeResult = null
            routeResults = emptyList()
            selectedRouteIndex = 0
            routeDestinationCoordinate = null
            routeDestinationName = ""
            routeStarted = false
            ActiveRouteSession.clear()
        }
    }

    LaunchedEffect(focusAlert?.id) {
        val target = focusAlert ?: return@LaunchedEffect
        val coordinate = LatLng(target.latitude, target.longitude)

        selectedAlert = target
        selectedPlace = null
        showSelectedPlaceCard = false
        searchSuggestions = emptyList()
        isSearchActive = false
        routePoints = emptyList()
        routeResult = null
        routeResults = emptyList()
        selectedRouteIndex = 0
        routeDestinationCoordinate = null
        routeDestinationName = ""
        mapCenter = coordinate
        mapZoom = 16.0
        onFocusHandled()
    }

    Scaffold(
        containerColor = MapBackground,
        bottomBar = {
            UrbanBottomBar(
                selectedItem = "Mapa",
                onItemSelected = onNavigate
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
                alerts = filteredAlerts,
                currentLocation = currentLocation,
                routePoints = routePoints,
                routeLines = routeResults.map { it.points },
                selectedRouteIndex = selectedRouteIndex,
                onAlertSelected = { alert ->
                    selectedAlert = alert
                    selectedPlace = null
                    showSelectedPlaceCard = false
                    routePoints = emptyList()
                    routeResult = null
                    routeResults = emptyList()
                    selectedRouteIndex = 0
                    routeDestinationCoordinate = null
                    routeDestinationName = ""
                    clearCarRouteSafely()
                    mapCenter = LatLng(alert.latitude, alert.longitude)
                    mapZoom = 15.0
                },
                onMapClick = { coordinate ->
                    selectPlaceFromMapTap(coordinate)
                },
                onCameraIdle = { coordinate ->
                    mapCenter = coordinate
                }
            )

            MapSearchPanel(
                searchQuery = searchQuery,
                isSearchActive = isSearchActive,
                onSearchQueryChange = {
                    searchQuery = it
                    isSearchActive = true
                },
                suggestions = searchSuggestions,
                selectedCategory = selectedCategory,
                selectedUrgency = selectedUrgency,
                totalAlerts = filteredAlerts.size,
                onSearch = { searchPlace() },
                onOpenFilters = { showFilters = true },
                onClearSearch = {
                    searchQuery = ""
                    searchSuggestions = emptyList()
                    isSearchActive = false
                },
                onSuggestionSelected = { moveToSearchResult(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            if (!isSearchActive && !isRouteOriginSearchActive) {
                MapControls(
                    onMyLocation = { requestCurrentLocation() },
                    onZoomIn = { mapZoom = (mapZoom + 1.0).coerceAtMost(19.0) },
                    onZoomOut = { mapZoom = (mapZoom - 1.0).coerceAtLeast(4.0) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 132.dp, end = 16.dp)
                )
            }

            if (showSelectedPlaceCard) selectedPlace?.let { place ->
                SelectedPlaceCard(
                    place = place,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    routeResult = routeResult,
                    routeResults = routeResults,
                    selectedRouteIndex = selectedRouteIndex,
                    originQuery = routeOriginQuery,
                    originSuggestions = routeOriginSuggestions,
                    isOriginSearchActive = isRouteOriginSearchActive,
                    isRouteLoading = isRouteLoading,
                    onClose = { closeSelectedPlaceCard() },
                    onOriginQueryChange = {
                        routeOriginQuery = it
                        isRouteOriginSearchActive = true
                    },
                    onClearOrigin = {
                        routeOriginQuery = currentLocation?.let { "Mi ubicación actual" } ?: MapDefaults.UtcjAddress
                        routeOriginSuggestions = emptyList()
                        isRouteOriginSearchActive = false
                        routeOriginCoordinate = currentLocation ?: MapDefaults.UtcjLocation
                        routePoints = emptyList()
                        routeResults = emptyList()
                        routeResult = null
                        routeDestinationCoordinate = null
                        routeDestinationName = ""
                        clearCarRouteSafely()
                    },
                    onOriginSelected = { origin ->
                        routeOriginQuery = origin.title
                        routeOriginCoordinate = origin.coordinate
                        routeOriginSuggestions = emptyList()
                        isRouteOriginSearchActive = false
                        routePoints = emptyList()
                        routeResults = emptyList()
                        routeResult = null
                        routeDestinationCoordinate = null
                        routeDestinationName = ""
                        clearCarRouteSafely()
                    },
                    onRouteSelected = { index ->
                        selectedRouteIndex = index
                        routeResult = routeResults.getOrNull(index)
                        routePoints = routeResults.getOrNull(index)?.points.orEmpty()
                        routeStarted = false
                    },
                    onRouteClick = { calculateRoute(place.coordinate) },
                    onShowSelectedRoute = {
                        val origin = routeOriginCoordinate ?: currentLocation ?: MapDefaults.UtcjLocation
                        val destination = routeDestinationCoordinate ?: place.coordinate
                        val routes = routeResults
                        val index = selectedRouteIndex
                        val destinationName = routeDestinationName.ifBlank { place.title }

                        showSelectedPlaceCard = false
                        routeStarted = true
                        saveRouteSession()

                        if (routes.isNotEmpty()) {
                            scope.launch {
                                try {
                                    carMapSyncRepository.publishRoute(
                                        routes = routes,
                                        selectedRouteIndex = index,
                                        origin = origin,
                                        destination = destination,
                                        destinationName = destinationName
                                    )
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        "No se pudo mandar esta ruta al auto.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }

            if (!showSelectedPlaceCard && routeStarted && routeResults.isNotEmpty()) {
                ActiveRouteSummaryCard(
                    route = routeResults.getOrNull(selectedRouteIndex) ?: routeResults.first(),
                    destinationName = routeDestinationName.ifBlank { selectedPlace?.title ?: "Destino seleccionado" },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    onOpenDetails = {
                        if (selectedPlace == null && routeDestinationCoordinate != null) {
                            selectedPlace = MapSearchResult(
                                title = routeDestinationName.ifBlank { "Destino seleccionado" },
                                subtitle = "Ruta activa",
                                coordinate = routeDestinationCoordinate ?: MapDefaults.UtcjLocation
                            )
                        }
                        showSelectedPlaceCard = true
                    },
                    onCancelRoute = { cancelActiveRoute() }
                )
            }

            selectedAlert?.let { alert ->
                SelectedAlertCard(
                    alert = alert,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    onClick = { onOpenAlert(alert) }
                )
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            FiltersSheetContent(
                selectedCategory = selectedCategory,
                selectedUrgency = selectedUrgency,
                onCategorySelected = {
                    selectedCategory = it
                    selectedAlert = null
                    publishFiltersSafely(category = it)
                },
                onUrgencySelected = {
                    selectedUrgency = it
                    selectedAlert = null
                    publishFiltersSafely(urgency = it)
                },
                onClearFilters = {
                    selectedCategory = "Todas"
                    selectedUrgency = "Todas"
                    selectedAlert = null
                    publishFiltersSafely(category = "Todas", urgency = "Todas")
                },
                onApply = { showFilters = false }
            )
        }
    }
}

@Composable
private fun MapSearchPanel(
    searchQuery: String,
    isSearchActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<MapSearchResult>,
    selectedCategory: String,
    selectedUrgency: String,
    totalAlerts: Int,
    onSearch: () -> Unit,
    onOpenFilters: () -> Unit,
    onClearSearch: () -> Unit,
    onSuggestionSelected: (MapSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.97f),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SearchInputBox(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    onSearch = onSearch,
                    onClear = onClearSearch,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = MapPrimary,
                    onClick = onOpenFilters,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        Text(
                            text = "Filtros",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Mostrando $totalAlerts alertas · $selectedCategory · $selectedUrgency",
                modifier = Modifier.padding(start = 4.dp, top = 9.dp),
                color = MapText.copy(alpha = 0.58f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isSearchActive && searchQuery.length >= 2) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, UrbanColors.Border),
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        if (suggestions.isEmpty()) {
                            SearchLoadingRow()
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
}

@Composable
private fun SearchInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, MapPrimary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MapText.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isBlank()) {
                    Text(
                        text = "Buscar dirección o lugar",
                        color = MapText.copy(alpha = 0.45f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MapText,
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
                    contentDescription = if (value.isBlank()) "Buscar" else "Limpiar búsqueda",
                    tint = MapPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchLoadingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MapText.copy(alpha = 0.42f),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Buscando lugares cercanos...",
            color = MapText.copy(alpha = 0.58f),
            fontSize = 13.sp
        )
    }
}
@Composable
private fun FiltersSheetContent(
    selectedCategory: String,
    selectedUrgency: String,
    onCategorySelected: (String) -> Unit,
    onUrgencySelected: (String) -> Unit,
    onClearFilters: () -> Unit,
    onApply: () -> Unit
) {
    val categories = listOf("Todas", "Bache", "Luminaria", "Residuos", "Tránsito", "Calle bloqueada")
    val urgencies = listOf("Todas", "Alta", "Media", "Baja")

    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Filtros",
                    color = MapText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Elige qué alertas quieres ver en el mapa.",
                    color = MapText.copy(alpha = 0.58f),
                    fontSize = 13.sp
                )
            }

            IconButton(onClick = onApply) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint = MapText
                )
            }
        }

        FilterPillSection(
            title = "Categoría",
            options = categories,
            selectedOption = selectedCategory,
            onOptionSelected = onCategorySelected
        )

        FilterPillSection(
            title = "Urgencia",
            options = urgencies,
            selectedOption = selectedUrgency,
            onOptionSelected = onUrgencySelected
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MapPrimary),
                border = BorderStroke(1.dp, MapPrimary)
            ) {
                Text("Limpiar")
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MapPrimary)
            ) {
                Text("Aplicar")
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = MapPrimary,
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.title,
                color = MapText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = suggestion.subtitle,
                color = MapText.copy(alpha = 0.56f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
private fun FilterPillSection(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            color = MapText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            options.chunked(2).forEach { rowOptions ->
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    rowOptions.forEach { option ->
                        FilterPill(
                            option = option,
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowOptions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    option: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) MapPrimary else Color.White,
        border = BorderStroke(1.dp, if (selected) MapPrimary else UrbanColors.Border),
        shadowElevation = if (selected) 2.dp else 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) Icons.Outlined.Check else iconForCategory(option),
                contentDescription = null,
                tint = if (selected) Color.White else MapText.copy(alpha = 0.72f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = option,
                color = if (selected) Color.White else MapText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
private fun SelectedAlertCard(
    alert: UrbanAlert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .padding(top = 9.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .background(color = UrbanColors.MapLine, shape = CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val urgencyColor = colorForUrgency(alert.urgency)

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .background(
                            color = urgencyColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconForCategory(alert.category),
                        contentDescription = null,
                        tint = urgencyColor,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = alert.title,
                            modifier = Modifier.weight(1f),
                            color = MapText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Surface(shape = RoundedCornerShape(7.dp), color = urgencyColor) {
                            Text(
                                text = alert.urgency,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    MapInformation(icon = Icons.Outlined.LocationOn, text = alert.address)
                    MapInformation(icon = Icons.Outlined.NearMe, text = alert.distanceText)
                    MapInformation(icon = Icons.Outlined.Schedule, text = alert.timeText)
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver alerta",
                    tint = MapText
                )
            }
        }
    }
}


@Composable
private fun SelectedPlaceCard(
    place: MapSearchResult,
    modifier: Modifier = Modifier,
    routeResult: MapRouteResult?,
    routeResults: List<MapRouteResult>,
    selectedRouteIndex: Int,
    originQuery: String,
    originSuggestions: List<MapSearchResult>,
    isOriginSearchActive: Boolean,
    isRouteLoading: Boolean,
    onClose: () -> Unit,
    onOriginQueryChange: (String) -> Unit,
    onClearOrigin: () -> Unit,
    onOriginSelected: (MapSearchResult) -> Unit,
    onRouteSelected: (Int) -> Unit,
    onRouteClick: () -> Unit,
    onShowSelectedRoute: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .background(color = UrbanColors.MapLine, shape = CircleShape)
                        .align(Alignment.Center)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar",
                        tint = MapHigh,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MapPrimary.copy(alpha = 0.10f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MapPrimary,
                        modifier = Modifier.padding(16.dp).size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.title,
                        color = MapText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = place.subtitle,
                        color = MapText.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    routeResult?.let { route ->
                        Text(
                            text = "Ruta: ${route.summaryText}",
                            color = MapPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Origen de ruta",
                color = MapText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchInputBox(
                value = originQuery,
                onValueChange = onOriginQueryChange,
                onSearch = {},
                onClear = onClearOrigin,
                modifier = Modifier.fillMaxWidth()
            )

            if (isOriginSearchActive && originQuery.length >= 2) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, UrbanColors.Border)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        if (originSuggestions.isEmpty()) {
                            Text(
                                text = "Buscando origen cercano...",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                color = MapText.copy(alpha = 0.58f),
                                fontSize = 12.sp
                            )
                        } else {
                            originSuggestions.forEach { suggestion ->
                                SearchSuggestionRow(
                                    suggestion = suggestion,
                                    onClick = { onOriginSelected(suggestion) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (routeResults.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    routeResults.forEachIndexed { index, route ->
                        RouteOptionRow(
                            routeNumber = index + 1,
                            route = route,
                            selected = index == selectedRouteIndex,
                            onClick = { onRouteSelected(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = if (routeResults.isEmpty()) onRouteClick else onShowSelectedRoute,
                enabled = !isRouteLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MapPrimary)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when {
                        isRouteLoading -> "Calculando rutas..."
                        routeResults.isEmpty() -> "Ver rutas"
                        else -> "Iniciar ruta"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RouteOptionRow(
    routeNumber: Int,
    route: MapRouteResult,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MapPrimary.copy(alpha = 0.10f) else UrbanColors.NeutralPanel,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MapPrimary else UrbanColors.Border
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) MapPrimary else MapText.copy(alpha = 0.12f)
            ) {
                Text(
                    text = routeNumber.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = if (selected) Color.White else MapText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = route.summaryText,
                    color = MapText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = routeSafetyText(route),
                    color = when {
                        route.blockedReports > 0 -> MapHigh
                        route.nearbyReports == 0 -> MapPrimary
                        else -> MapMedium
                    },
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Ruta seleccionada",
                    tint = MapPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveRouteSummaryCard(
    route: MapRouteResult,
    destinationName: String,
    modifier: Modifier = Modifier,
    onOpenDetails: () -> Unit,
    onCancelRoute: () -> Unit
) {
    Card(
        onClick = onOpenDetails,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MapPrimary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    tint = MapPrimary,
                    modifier = Modifier.padding(11.dp).size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ruta activa",
                    color = MapText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = destinationName,
                    color = MapText.copy(alpha = 0.60f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.widthIn(min = 96.dp, max = 132.dp),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = onCancelRoute,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cancelar ruta",
                        tint = MapHigh,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = route.summaryText,
                    color = MapPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = routeSafetyText(route),
                    color = when {
                        route.blockedReports > 0 -> MapHigh
                        route.nearbyReports == 0 -> MapPrimary
                        else -> MapMedium
                    },
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 5.dp,
            onClick = onMyLocation
        ) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = "Mi ubicación",
                tint = MapText,
                modifier = Modifier.padding(13.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 5.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onZoomIn) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Acercar",
                        tint = MapText
                    )
                }

                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(UrbanColors.Border)
                )

                IconButton(onClick = onZoomOut) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = "Alejar",
                        tint = MapText
                    )
                }
            }
        }
    }
}

@Composable
private fun MapControl(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 5.dp,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MapText,
            modifier = Modifier.padding(13.dp)
        )
    }
}
@Composable
private fun MapInformation(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MapPrimary,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            color = MapText.copy(alpha = 0.68f),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun iconForCategory(category: String): ImageVector {
    val normalized = category.lowercase()
    return when {
        normalized.contains("bache") || normalized.contains("via") || normalized.contains("vía") -> Icons.Outlined.Construction
        normalized.contains("luminaria") || normalized.contains("ilumin") -> Icons.Outlined.Lightbulb
        normalized.contains("residuo") || normalized.contains("basura") -> Icons.Outlined.Delete
        normalized.contains("transito") || normalized.contains("tránsito") || normalized.contains("choque") || normalized.contains("veh") -> Icons.Outlined.DirectionsCar
        normalized.contains("bloque") || normalized.contains("calle") || normalized.contains("cerrada") -> Icons.Outlined.Traffic
        normalized.contains("seguridad") || normalized.contains("riesgo") -> Icons.Outlined.Shield
        normalized == "alta" || normalized == "media" || normalized == "baja" -> Icons.Outlined.ReportProblem
        else -> Icons.Outlined.Traffic
    }
}

private fun matchesCategoryFilter(alert: UrbanAlert, selectedCategory: String): Boolean {
    if (selectedCategory == "Todas") return true

    val normalized = "${alert.category} ${alert.title} ${alert.description}".lowercase()
    return when (selectedCategory) {
        "Vía pública" -> normalized.contains("bache") ||
            normalized.contains("via") ||
            normalized.contains("vía") ||
            normalized.contains("bloque") ||
            normalized.contains("cerrada") ||
            normalized.contains("residuo") ||
            normalized.contains("basura")
        else -> alert.category.equals(selectedCategory, ignoreCase = true) ||
            alert.title.equals(selectedCategory, ignoreCase = true)
    }
}

private fun colorForUrgency(urgency: String): Color {
    return when (urgency) {
        "Alta" -> MapHigh
        "Media" -> MapMedium
        "Baja" -> MapLow
        else -> MapPrimary
    }
}

private fun routeSafetyText(route: MapRouteResult): String {
    return when {
        route.blockedReports > 0 -> "${route.blockedReports} calle(s) bloqueada(s) · no recomendable"
        route.nearbyReports == 0 -> "Sin alertas cercanas · ruta libre"
        else -> "${route.nearbyReports} alerta(s) cerca · sin calles bloqueadas"
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

private fun countBlockedStreetReportsNearRoute(
    routePoints: List<LatLng>,
    alerts: List<UrbanAlert>,
    thresholdMeters: Double = 90.0
): Int {
    if (routePoints.size < 2) return 0

    return alerts.count { alert ->
        if (!isBlockedStreetAlert(alert)) return@count false

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

private fun isBlockedStreetAlert(alert: UrbanAlert): Boolean {
    val text = "${alert.title} ${alert.category} ${alert.description}".lowercase()
    return text.contains("calle bloqueada") ||
        text.contains("calle cerrada") ||
        text.contains("bloquead") ||
        text.contains("cerrad") ||
        text.contains("obstruid")
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

private fun formatDistance(distanceMeters: Double): String {
    return if (distanceMeters < 1000.0) {
        "A ${distanceMeters.toInt()} m"
    } else {
        "A ${String.format("%.1f", distanceMeters / 1000.0)} km"
    }
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
private fun MapScreenPreview() {
    AlertasUrbanasTheme {
        MapScreen()
    }
}





