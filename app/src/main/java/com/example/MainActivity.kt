package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.receiver.WeatherAlertReceiver
import com.example.ui.screens.WeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModel.Factory(applicationContext)
    }

    // Permission launcher to request location and push notifications
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true // default to true on older APIs

        Log.d("MainActivity", "Permissions updated: Fine Location=$fineGranted, Coarse Location=$coarseGranted, Notification=$notificationGranted) ")

        if (fineGranted || coarseGranted) {
            fetchLocationAndLoad()
        } else {
            // Fallback to default city if permission denied
            viewModel.fetchWeather(37.7749, -122.4194, this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge full screen experience
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Schedule periodic background checks for precipitation warnings
        try {
            WeatherAlertReceiver.scheduleHourlyAlerts(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to schedule hourly alerts: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                WeatherScreen(
                    viewModel = viewModel,
                    onRequestCurrentLocation = { requestPermissionsAndLoad() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun requestPermissionsAndLoad() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private fun fetchLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsAndLoad()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    viewModel.fetchWeather(location.latitude, location.longitude, this)
                } else {
                    // Fallback if location is null (common in emulators/previews without GPS fixes)
                    viewModel.fetchWeather(37.7749, -122.4194, this)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error retrieving location: ${e.message}", e)
                // Fallback to San Francisco
                viewModel.fetchWeather(37.7749, -122.4194, this)
            }
    }
}
