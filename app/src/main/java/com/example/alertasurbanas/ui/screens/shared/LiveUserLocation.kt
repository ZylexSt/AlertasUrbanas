package com.example.alertasurbanas.ui.screens.shared

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.geometry.LatLng

@Composable
fun rememberLiveUserLocation(
    autoRequestPermission: Boolean = true,
    intervalMillis: Long = 5_000L,
    minUpdateDistanceMeters: Float = 5f
): State<LatLng?> {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationState = remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(autoRequestPermission, hasPermission) {
        if (autoRequestPermission && !hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(hasPermission, intervalMillis, minUpdateDistanceMeters) {
        if (!hasPermission) {
            onDispose { }
        } else {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                intervalMillis
            )
                .setMinUpdateIntervalMillis(2_000L)
                .setMinUpdateDistanceMeters(minUpdateDistanceMeters)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        locationState.value = LatLng(location.latitude, location.longitude)
                    }
                }
            }

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        locationState.value = LatLng(it.latitude, it.longitude)
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
            } catch (_: SecurityException) {
                hasPermission = false
            }

            onDispose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }

    return locationState
}
