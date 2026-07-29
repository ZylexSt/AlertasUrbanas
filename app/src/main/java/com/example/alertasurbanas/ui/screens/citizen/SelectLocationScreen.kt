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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
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
import com.example.alertasurbanas.data.MapSearchResult
import com.example.alertasurbanas.data.MapSearchService
import com.example.alertasurbanas.ui.screens.shared.MapTilerMap
import com.example.alertasurbanas.ui.theme.AlertasUrbanasTheme
import com.example.alertasurbanas.ui.theme.UrbanColors
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

private val LocationPrimary = UrbanColors.Primary
private val LocationAccent = UrbanColors.Terracotta
private val LocationText = UrbanColors.TextPrimary

@Composable
fun SelectLocationScreen(
    initialAddress: String = "Av. Independencia 250, Col. Centro",
    onBack: () -> Unit = {},
    onConfirm: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<MapSearchResult>>(emptyList()) }
    var mapCenter by remember { mutableStateOf(LatLng(31.761, -106.485)) }
    var selectedCenter by remember { mutableStateOf(mapCenter) }
    var selectedAddress by remember { mutableStateOf(initialAddress) }
    var mapZoom by remember { mutableStateOf(16.0) }

    fun moveToResult(result: MapSearchResult) {
        searchQuery = result.title
        suggestions = emptyList()
        selectedCenter = result.coordinate
        mapCenter = result.coordinate
        mapZoom = 17.0
        selectedAddress = result.subtitle.substringBefore(" · ")
    }

    fun searchPlace() {
        if (searchQuery.isBlank()) {
            Toast.makeText(context, "Escribe una dirección o lugar.", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            val result = MapSearchService.searchResults(searchQuery, limit = 1, proximity = selectedCenter).firstOrNull()
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
                        selectedCenter = coordinate
                        mapCenter = coordinate
                        mapZoom = 17.0
                    } else {
                        Toast.makeText(context, "En emulador asigna una ubicación simulada.", Toast.LENGTH_LONG).show()
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

        if (hasPermission) centerOnDeviceLocation() else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(searchQuery, selectedCenter) {
        if (searchQuery.length < 2) {
            suggestions = emptyList()
            return@LaunchedEffect
        }

        delay(350)
        suggestions = MapSearchService.searchResults(searchQuery, limit = 5, proximity = selectedCenter)
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
                onConfirm = { onConfirm(selectedAddress) }
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
                onCameraIdle = { coordinate ->
                    selectedCenter = coordinate
                }
            )

            LocationSearchHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                suggestions = suggestions,
                onBack = onBack,
                onSearch = { searchPlace() },
                onClearSearch = {
                    searchQuery = ""
                    suggestions = emptyList()
                },
                onSuggestionSelected = { moveToResult(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            Surface(
                onClick = { requestCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 92.dp, end = 16.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 5.dp
            ) {
                Icon(
                    imageVector = Icons.Outlined.MyLocation,
                    contentDescription = "Mi ubicación",
                    tint = LocationText,
                    modifier = Modifier.padding(13.dp)
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

                if (searchQuery.length >= 2) {
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

            Spacer(modifier = Modifier.height(17.dp))

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SelectLocationPreview() {
    AlertasUrbanasTheme {
        SelectLocationScreen()
    }
}
