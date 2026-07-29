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

private val MapBackground = UrbanColors.MapBackground
private val MapPrimary = UrbanColors.Primary
private val MapText = UrbanColors.TextPrimary
private val MapHigh = UrbanColors.HighUrgency
private val MapMedium = UrbanColors.MediumUrgency
private val MapLow = UrbanColors.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigate: (String) -> Unit = {},
    onOpenAlert: (UrbanAlert) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedUrgency by remember { mutableStateOf("Todas") }
    var selectedAlert by remember { mutableStateOf<UrbanAlert?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var mapCenter by remember { mutableStateOf(LatLng(31.761, -106.485)) }
    var mapZoom by remember { mutableStateOf(13.0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allAlerts = remember { AlertRepository.publicAlerts }
    val filteredAlerts = allAlerts.filter { alert ->
        (selectedCategory == "Todas" || alert.category == selectedCategory) &&
            (selectedUrgency == "Todas" || alert.urgency == selectedUrgency)
    }

    fun moveToSearchResult(result: MapSearchResult) {
        searchQuery = result.title
        searchSuggestions = emptyList()
        mapCenter = result.coordinate
        mapZoom = 15.0
        selectedAlert = null
    }

    fun centerOnDeviceLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        mapCenter = LatLng(location.latitude, location.longitude)
                        mapZoom = 15.0
                        selectedAlert = null
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

    LaunchedEffect(searchQuery) {
        if (searchQuery.length < 2) {
            searchSuggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        searchSuggestions = MapSearchService.searchResults(searchQuery, limit = 5, proximity = mapCenter)
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
                onAlertSelected = { alert ->
                    selectedAlert = alert
                    mapCenter = LatLng(alert.latitude, alert.longitude)
                    mapZoom = 15.0
                }
            )

            MapSearchPanel(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                suggestions = searchSuggestions,
                selectedCategory = selectedCategory,
                selectedUrgency = selectedUrgency,
                totalAlerts = filteredAlerts.size,
                onSearch = { searchPlace() },
                onOpenFilters = { showFilters = true },
                onClearSearch = {
                    searchQuery = ""
                    searchSuggestions = emptyList()
                },
                onSuggestionSelected = { moveToSearchResult(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            MapControl(
                icon = Icons.Outlined.MyLocation,
                description = "Mi ubicación",
                onClick = { requestCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 104.dp, end = 16.dp)
            )

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

            if (searchQuery.length >= 2) {
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
                    MapInformation(icon = Icons.Outlined.NearMe, text = "A ${alert.distanceText}")
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MapScreenPreview() {
    AlertasUrbanasTheme {
        MapScreen()
    }
}





