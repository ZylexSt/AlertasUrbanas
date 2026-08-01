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
import com.example.alertasurbanas.data.MapDefaults
import com.example.alertasurbanas.data.MapRouteResult
import com.example.alertasurbanas.data.MapRouteService
import com.example.alertasurbanas.data.MapSearchResult
import com.example.alertasurbanas.data.MapSearchService
import com.example.alertasurbanas.data.UrbanAlert
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    alerts: List<UrbanAlert> = emptyList(),
    onNavigate: (String) -> Unit = {},
    onOpenAlert: (UrbanAlert) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

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
    var currentLocation by remember { mutableStateOf<LatLng?>(MapDefaults.UtcjLocation) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeResult by remember { mutableStateOf<MapRouteResult?>(null) }
    var routeResults by remember { mutableStateOf<List<MapRouteResult>>(emptyList()) }
    var selectedRouteIndex by remember { mutableStateOf(0) }
    var isRouteLoading by remember { mutableStateOf(false) }
    var routeOriginQuery by remember { mutableStateOf("Mi ubicación actual") }
    var routeOriginSuggestions by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var isRouteOriginSearchActive by remember { mutableStateOf(false) }
    var routeOriginCoordinate by remember { mutableStateOf<LatLng?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val distanceOrigin = currentLocation ?: MapDefaults.UtcjLocation
    val allAlerts = (if (alerts.isNotEmpty()) alerts else AlertRepository.publicAlerts)
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
        (selectedCategory == "Todas" || alert.category == selectedCategory) &&
            (selectedUrgency == "Todas" || alert.urgency == selectedUrgency)
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
    }

    fun centerOnDeviceLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val coordinate = LatLng(location.latitude, location.longitude)
                        currentLocation = coordinate
                        routeOriginCoordinate = coordinate
                        routeOriginQuery = "Mi ubicación actual"
                        mapCenter = coordinate
                        mapZoom = 16.0
                        selectedAlert = null
                        selectedPlace = null
                        showSelectedPlaceCard = false
                        routePoints = emptyList()
                        routeResult = null
                        routeResults = emptyList()
                        selectedRouteIndex = 0
                    } else {
                        Toast.makeText(
                            context,
                            "No se pudo obtener tu ubicación todavía. En emulador asigna una ubicación simulada.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "No se pudo obtener tu ubicación.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (_: SecurityException) {
            Toast.makeText(context, "Activa el permiso de ubicación.", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            centerOnDeviceLocation()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestCurrentLocation() {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission) {
            centerOnDeviceLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun searchPlace() {
        if (searchQuery.isBlank()) {
            Toast.makeText(context, "Escribe una dirección o lugar.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val result = MapSearchService.searchResults(searchQuery, limit = 1, proximity = mapCenter).firstOrNull()
            if (result != null) {
                moveToSearchResult(result)
            } else {
                Toast.makeText(context, "No encontré esa dirección.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun calculateRoute(destination: LatLng) {
        val origin = routeOriginCoordinate ?: currentLocation ?: MapDefaults.UtcjLocation

        scope.launch {
            isRouteLoading = true
            val results = MapRouteService.drivingRoutes(origin, destination, targetCount = 3)
                .map { route ->
                    route.withNearbyReports(countReportsNearRoute(route.points, filteredAlerts))
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

    LaunchedEffect(searchQuery, isSearchActive, currentLocation) {
        if (!isSearchActive || searchQuery.length < 2) {
            searchSuggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        searchSuggestions = MapSearchService.searchResults(
            query = searchQuery,
            limit = 5,
            proximity = currentLocation ?: MapDefaults.UtcjLocation,
            radiusMeters = 12_000
        )
    }

    LaunchedEffect(routeOriginQuery, isRouteOriginSearchActive, currentLocation) {
        if (!isRouteOriginSearchActive || routeOriginQuery.length < 2) {
            routeOriginSuggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        routeOriginSuggestions = MapSearchService.searchResults(
            query = routeOriginQuery,
            limit = 4,
            proximity = currentLocation ?: MapDefaults.UtcjLocation,
            radiusMeters = 20_000
        )
    }

    LaunchedEffect(Unit) {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFinePermission) {
            centerOnDeviceLocation()
        }
    }

    LaunchedEffect(filteredAlerts, selectedAlert) {
        if (selectedAlert != null && filteredAlerts.none { it.id == selectedAlert?.id }) {
            selectedAlert = null
        }
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
                    mapCenter = LatLng(alert.latitude, alert.longitude)
                    mapZoom = 15.0
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
                    onOriginQueryChange = {
                        routeOriginQuery = it
                        isRouteOriginSearchActive = true
                    },
                    onClearOrigin = {
                        routeOriginQuery = "Mi ubicación actual"
                        routeOriginSuggestions = emptyList()
                        isRouteOriginSearchActive = false
                        routeOriginCoordinate = currentLocation ?: MapDefaults.UtcjLocation
                        routePoints = emptyList()
                        routeResults = emptyList()
                        routeResult = null
                    },
                    onOriginSelected = { origin ->
                        routeOriginQuery = origin.title
                        routeOriginCoordinate = origin.coordinate
                        routeOriginSuggestions = emptyList()
                        isRouteOriginSearchActive = false
                        routePoints = emptyList()
                        routeResults = emptyList()
                        routeResult = null
                    },
                    onRouteSelected = { index ->
                        selectedRouteIndex = index
                        routeResult = routeResults.getOrNull(index)
                        routePoints = routeResults.getOrNull(index)?.points.orEmpty()
                    },
                    onRouteClick = { calculateRoute(place.coordinate) },
                    onShowSelectedRoute = { showSelectedPlaceCard = false }
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
                },
                onUrgencySelected = {
                    selectedUrgency = it
                    selectedAlert = null
                },
                onClearFilters = {
                    selectedCategory = "Todas"
                    selectedUrgency = "Todas"
                    selectedAlert = null
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
    val categories = listOf("Todas", "Tránsito", "Vía pública", "Iluminación", "Seguridad", "Incendio")
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
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .background(color = UrbanColors.MapLine, shape = CircleShape)
                    .align(Alignment.CenterHorizontally)
            )

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
                        else -> "Ver ruta seleccionada"
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
                    text = "${route.nearbyReports} reporte(s) cerca de esta ruta",
                    color = if (route.nearbyReports == 0) MapPrimary else MapHigh,
                    fontSize = 11.sp,
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
    return when (category) {
        "Tránsito" -> Icons.Outlined.DirectionsCar
        "Vía pública" -> Icons.Outlined.Construction
        "Iluminación" -> Icons.Outlined.Lightbulb
        "Seguridad" -> Icons.Outlined.Shield
        "Incendio" -> Icons.Outlined.LocalFireDepartment
        "Alta", "Media", "Baja" -> Icons.Outlined.ReportProblem
        else -> Icons.Outlined.Traffic
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





