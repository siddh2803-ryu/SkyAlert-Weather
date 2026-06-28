package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeatherCodeUtils
import com.example.data.repository.WeatherData
import com.example.ui.viewmodel.WeatherUiState
import com.example.ui.viewmodel.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onRequestCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cachedWeather by viewModel.cachedWeather.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var isCelsius by remember { mutableStateOf(true) }

    val presetLocations = listOf(
        PresetCity("San Francisco", 37.7749, -122.4194),
        PresetCity("New York", 40.7128, -74.0060),
        PresetCity("London", 51.5074, -0.1278),
        PresetCity("Tokyo", 35.6762, 139.6503),
        PresetCity("Sydney", -33.8688, 151.2093)
    )

    LaunchedEffect(cachedWeather) {
        if (cachedWeather == null && uiState is WeatherUiState.Idle) {
            onRequestCurrentLocation()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRequestCurrentLocation,
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                modifier = Modifier.testTag("refresh_location_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Location"
                )
            }
        },
        containerColor = Color(0xFF0B131E),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val weatherCode = cachedWeather?.current?.weatherCode ?: 0
            AnimatedWeatherBackground(weatherCode = weatherCode)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SKYALERT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6),
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "PREVENTIVE WEATHER ALERTING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "°C",
                            color = if (isCelsius) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isCelsius) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { isCelsius = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("celsius_toggle")
                        )
                        Text(
                            text = "°F",
                            color = if (!isCelsius) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (!isCelsius) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { isCelsius = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("fahrenheit_toggle")
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    item {
                        PresetChip(
                            label = "Current Location",
                            isSelected = false,
                            onClick = onRequestCurrentLocation,
                            iconColor = Color(0xFF60A5FA)
                        )
                    }
                    itemsIndexed(presetLocations) { _, city ->
                        val isCurrentlySelected = cachedWeather?.let {
                            Math.abs(it.latitude - city.latitude) < 0.05 && Math.abs(it.longitude - city.longitude) < 0.05
                        } ?: false
                        PresetChip(
                            label = city.name,
                            isSelected = isCurrentlySelected,
                            onClick = {
                                viewModel.fetchWeather(city.latitude, city.longitude, context)
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = uiState is WeatherUiState.Loading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Synchronizing local precipitation models...",
                            color = Color(0xFF93C5FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState is WeatherUiState.Error) {
                    val errorState = uiState as WeatherUiState.Error
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.10f)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.30f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFFCA5A5)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorState.message,
                                color = Color(0xFFFEE2E2),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                val data = cachedWeather
                if (data != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            WeatherHeroSection(data = data, isCelsius = isCelsius)
                        }

                        item {
                            NotificationTestCard(
                                onTestTrigger = { viewModel.triggerTestNotification(context) }
                            )
                        }

                        item {
                            WeatherGridMetrics(data = data, isCelsius = isCelsius)
                        }

                        item {
                            PrecipitationTimelineSection(data = data, isCelsius = isCelsius)
                        }

                        item {
                            DailyForecastSection(data = data, isCelsius = isCelsius)
                        }

                        item {
                            WidgetPreviewCard()
                        }
                    }
                } else {
                    if (uiState !is WeatherUiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Search",
                                    tint = Color(0xFF00E5FF).copy(alpha = 0.3f),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Weather Data Cached",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Grant location permission or choose a preset city above to sync live local weather forecasts.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF78909C),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                ElevatedButton(
                                    onClick = onRequestCurrentLocation,
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF0D1117)
                                    ),
                                    modifier = Modifier.testTag("init_fetch_button")
                                ) {
                                    Text("Grant Permission & Load")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconColor: Color? = null
) {
    Surface(
        color = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF60A5FA).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.10f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("preset_$label")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconColor != null) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp).padding(end = 4.dp)
                )
            }
            Text(
                text = label,
                color = if (isSelected) Color(0xFF93C5FD) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeatherHeroSection(data: WeatherData, isCelsius: Boolean) {
    val temp = if (isCelsius) data.current.temperature2m else (data.current.temperature2m * 9 / 5 + 32)
    val apparent = if (isCelsius) data.current.apparentTemperature else (data.current.apparentTemperature * 9 / 5 + 32)
    val unitStr = if (isCelsius) "°C" else "°F"

    val descStr = WeatherCodeUtils.getDescription(data.current.weatherCode)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = data.areaName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFFE2E2E6),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeatherSymbolCanvas(weatherCode = data.current.weatherCode, modifier = Modifier.size(120.dp))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${temp.toInt()}$unitStr",
            fontSize = 88.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            letterSpacing = (-4).sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Text(
            text = "$descStr  •  Feels like ${apparent.toInt()}$unitStr",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF93C5FD).copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val updatedStr = dateFormat.format(Date(data.cachedTime))
        Text(
            text = "Updated $updatedStr",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun NotificationTestCard(onTestTrigger: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("notification_tester_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3B82F6).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification Alerts",
                    tint = Color(0xFF93C5FD)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rain Alert",
                        color = Color(0xFFDBEAFE),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Now",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Hourly precipitation warnings active. Tap TEST to simulate a background warning scan.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onTestTrigger,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF60A5FA)),
                modifier = Modifier.testTag("test_push_button")
            ) {
                Text(
                    text = "TEST",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WeatherGridMetrics(data: WeatherData, isCelsius: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricItem(
            title = "Humidity",
            value = "${data.current.relativeHumidity2m}%",
            icon = {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, 2f)
                        quadraticTo(size.width - 2f, size.height * 0.7f, size.width / 2f, size.height - 2f)
                        quadraticTo(2f, size.height * 0.7f, size.width / 2f, 2f)
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF60A5FA)
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )

        MetricItem(
            title = "Wind",
            value = "${data.current.windSpeed10m.toInt()} km/h",
            icon = {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawLine(
                        color = Color(0xFF60A5FA),
                        start = Offset(2f, size.height * 0.3f),
                        end = Offset(size.width - 4f, size.height * 0.3f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF60A5FA),
                        start = Offset(4f, size.height * 0.6f),
                        end = Offset(size.width - 2f, size.height * 0.6f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )

        MetricItem(
            title = "Rain Today",
            value = "${data.current.precipitation} mm",
            icon = {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawLine(
                        color = Color(0xFF60A5FA),
                        start = Offset(size.width * 0.3f, 2f),
                        end = Offset(size.width * 0.2f, size.height - 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF60A5FA),
                        start = Offset(size.width * 0.7f, 2f),
                        end = Offset(size.width * 0.6f, size.height - 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricItem(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PrecipitationTimelineSection(data: WeatherData, isCelsius: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hourly Precipitation Outlook",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF60A5FA))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rain %",
                        color = Color(0xFF60A5FA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("h a", Locale.getDefault())

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val hourlySize = Math.min(data.hourly.time.size, 24)
                items(hourlySize) { index ->
                    val rawTime = data.hourly.time[index]
                    val formattedTime = try {
                        val parsed = sdfInput.parse(rawTime)
                        if (parsed != null) sdfOutput.format(parsed) else rawTime
                    } catch (e: Exception) {
                        rawTime.substringAfter("T")
                    }

                    val hourTemp = if (isCelsius) data.hourly.temperature2m[index] else (data.hourly.temperature2m[index] * 9 / 5 + 32)
                    val hourProb = data.hourly.precipitationProbability[index]
                    val hourCode = data.hourly.weatherCode[index]

                    val isHighRain = hourProb >= 30
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isHighRain) Color.White.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isHighRain) Color.White.copy(alpha = 0.20f)
                                    else Color.White.copy(alpha = 0.05f)
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            color = if (isHighRain) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        WeatherSymbolCanvas(weatherCode = hourCode, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${hourTemp.toInt()}°",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$hourProb%",
                            color = if (isHighRain) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = if (isHighRain) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastSection(data: WeatherData, isCelsius: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7-Day Precipitation Outlook",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfDay = SimpleDateFormat("EEEE", Locale.getDefault())

            val dailySize = data.daily.time.size
            for (i in 0 until dailySize) {
                val rawDate = data.daily.time[i]
                val dayOfWeek = try {
                    val parsed = sdfInput.parse(rawDate)
                    if (parsed != null) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        if (rawDate == today) "Today" else sdfDay.format(parsed)
                    } else rawDate
                } catch (e: Exception) {
                    rawDate
                }

                val maxTemp = if (isCelsius) data.daily.temperature2mMax[i] else (data.daily.temperature2mMax[i] * 9 / 5 + 32)
                val minTemp = if (isCelsius) data.daily.temperature2mMin[i] else (data.daily.temperature2mMin[i] * 9 / 5 + 32)
                val precipProb = data.daily.precipitationProbabilityMax[i]
                val dCode = data.daily.weatherCode[i]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayOfWeek,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(90.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        WeatherSymbolCanvas(weatherCode = dCode, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = WeatherCodeUtils.getDescription(dCode),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = if (precipProb > 0) "$precipProb% rain" else "Dry",
                        color = if (precipProb >= 30) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = if (precipProb >= 30) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${maxTemp.toInt()}° / ${minTemp.toInt()}°",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(65.dp),
                        textAlign = TextAlign.End
                    )
                }

                if (i < dailySize - 1) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherSymbolCanvas(weatherCode: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (weatherCode) {
            0 -> {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = w * 0.35f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.3f),
                    radius = w * 0.45f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            1, 2, 3 -> {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = w * 0.28f,
                    center = Offset(cx - w * 0.15f, cy - h * 0.1f)
                )
                val cloudPath = Path().apply {
                    moveTo(cx - w * 0.2f, cy + h * 0.25f)
                    lineTo(cx + w * 0.25f, cy + h * 0.25f)
                    cubicTo(cx + w * 0.45f, cy + h * 0.25f, cx + w * 0.45f, cy - h * 0.05f, cx + w * 0.2f, cy - h * 0.05f)
                    cubicTo(cx + w * 0.15f, cy - h * 0.28f, cx - w * 0.15f, cy - h * 0.28f, cx - w * 0.15f, cy - h * 0.08f)
                    cubicTo(cx - w * 0.4f, cy - h * 0.08f, cx - w * 0.4f, cy + h * 0.25f, cx - w * 0.2f, cy + h * 0.25f)
                }
                drawPath(
                    path = cloudPath,
                    color = Color(0xFFECEFF1)
                )
            }
            45, 48 -> {
                drawLine(
                    color = Color(0xFFB0BEC5),
                    start = Offset(w * 0.15f, cy - h * 0.15f),
                    end = Offset(w * 0.85f, cy - h * 0.15f),
                    strokeWidth = 3.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFB0BEC5),
                    start = Offset(w * 0.25f, cy),
                    end = Offset(w * 0.75f, cy),
                    strokeWidth = 3.dp.toPx()
                )
                drawLine(
                    color = Color(0xFFB0BEC5),
                    start = Offset(w * 0.15f, cy + h * 0.15f),
                    end = Offset(w * 0.85f, cy + h * 0.15f),
                    strokeWidth = 3.dp.toPx()
                )
            }
            61, 63, 65, 80, 81, 82 -> {
                val cloudPath = Path().apply {
                    moveTo(cx - w * 0.25f, cy + h * 0.1f)
                    lineTo(cx + w * 0.25f, cy + h * 0.1f)
                    cubicTo(cx + w * 0.45f, cy + h * 0.1f, cx + w * 0.45f, cy - h * 0.2f, cx + w * 0.15f, cy - h * 0.2f)
                    cubicTo(cx + w * 0.1f, cy - h * 0.4f, cx - w * 0.2f, cy - h * 0.4f, cx - w * 0.2f, cy - h * 0.2f)
                    cubicTo(cx - w * 0.45f, cy - h * 0.2f, cx - w * 0.45f, cy + h * 0.1f, cx - w * 0.25f, cy + h * 0.1f)
                }
                drawPath(
                    path = cloudPath,
                    color = Color(0xFF90A4AE)
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx - w * 0.15f, cy + h * 0.18f),
                    end = Offset(cx - w * 0.22f, cy + h * 0.42f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx, cy + h * 0.22f),
                    end = Offset(cx - w * 0.07f, cy + h * 0.46f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF00E5FF),
                    start = Offset(cx + w * 0.15f, cy + h * 0.18f),
                    end = Offset(cx + w * 0.08f, cy + h * 0.42f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            71, 73, 75, 77, 85, 86 -> {
                val cloudPath = Path().apply {
                    moveTo(cx - w * 0.25f, cy + h * 0.1f)
                    lineTo(cx + w * 0.25f, cy + h * 0.1f)
                    cubicTo(cx + w * 0.45f, cy + h * 0.1f, cx + w * 0.45f, cy - h * 0.2f, cx + w * 0.15f, cy - h * 0.2f)
                    cubicTo(cx + w * 0.1f, cy - h * 0.4f, cx - w * 0.2f, cy - h * 0.4f, cx - w * 0.2f, cy - h * 0.2f)
                    cubicTo(cx - w * 0.45f, cy - h * 0.2f, cx - w * 0.45f, cy + h * 0.1f, cx - w * 0.25f, cy + h * 0.1f)
                }
                drawPath(
                    path = cloudPath,
                    color = Color(0xFFB0BEC5)
                )
                drawCircle(color = Color.White, radius = 2.5f.dp.toPx(), center = Offset(cx - w * 0.15f, cy + h * 0.25f))
                drawCircle(color = Color.White, radius = 2.5f.dp.toPx(), center = Offset(cx, cy + h * 0.3f))
                drawCircle(color = Color.White, radius = 2.5f.dp.toPx(), center = Offset(cx + w * 0.15f, cy + h * 0.25f))
            }
            95, 96, 99 -> {
                val cloudPath = Path().apply {
                    moveTo(cx - w * 0.25f, cy + h * 0.1f)
                    lineTo(cx + w * 0.25f, cy + h * 0.1f)
                    cubicTo(cx + w * 0.45f, cy + h * 0.1f, cx + w * 0.45f, cy - h * 0.2f, cx + w * 0.15f, cy - h * 0.2f)
                    cubicTo(cx + w * 0.1f, cy - h * 0.4f, cx - w * 0.2f, cy - h * 0.4f, cx - w * 0.2f, cy - h * 0.2f)
                    cubicTo(cx - w * 0.45f, cy - h * 0.2f, cx - w * 0.45f, cy + h * 0.1f, cx - w * 0.25f, cy + h * 0.1f)
                }
                drawPath(
                    path = cloudPath,
                    color = Color(0xFF546E7A)
                )
                val boltPath = Path().apply {
                    moveTo(cx, cy + h * 0.12f)
                    lineTo(cx - w * 0.1f, cy + h * 0.28f)
                    lineTo(cx + w * 0.04f, cy + h * 0.28f)
                    lineTo(cx - w * 0.04f, cy + h * 0.46f)
                    lineTo(cx + w * 0.15f, cy + h * 0.22f)
                    lineTo(cx, cy + h * 0.22f)
                }
                drawPath(
                    path = boltPath,
                    color = Color(0xFFFFEE58)
                )
            }
            else -> {
                val cloudPath = Path().apply {
                    moveTo(cx - w * 0.25f, cy + h * 0.15f)
                    lineTo(cx + w * 0.25f, cy + h * 0.15f)
                    cubicTo(cx + w * 0.45f, cy + h * 0.15f, cx + w * 0.45f, cy - h * 0.15f, cx + w * 0.15f, cy - h * 0.15f)
                    cubicTo(cx + w * 0.1f, cy - h * 0.38f, cx - w * 0.2f, cy - h * 0.38f, cx - w * 0.2f, cy - h * 0.15f)
                    cubicTo(cx - w * 0.45f, cy - h * 0.15f, cx - w * 0.45f, cy + h * 0.15f, cx - w * 0.25f, cy + h * 0.15f)
                }
                drawPath(
                    path = cloudPath,
                    color = Color(0xFFECEFF1)
                )
            }
        }
    }
}

@Composable
fun AnimatedWeatherBackground(weatherCode: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Draw the Atmospheric Background Gradient from HTML
        // bg-radial-[at_20%_20%] from-[#1E293B] via-[#0B131E] to-[#020617]
        val centerOffset = Offset(w * 0.20f, h * 0.20f)
        val maxDim = Math.max(w, h)
        
        val radBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF1E293B), // #1E293B
                Color(0xFF0B131E), // #0B131E
                Color(0xFF020617)  // #020617
            ),
            center = centerOffset,
            radius = maxDim * 1.5f
        )
        drawRect(brush = radBrush)

        // 2. Draw the Atmospheric Glowing Light Blob from HTML
        // right-[-20%] top-20 w-64 h-64 bg-blue-500/20 rounded-full blur-3xl
        val glowCenter = Offset(w * 1.15f, h * 0.15f)
        val glowRadius = h * 0.40f
        val glowBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF3B82F6).copy(alpha = 0.20f), // blue-500/20
                Color.Transparent
            ),
            center = glowCenter,
            radius = glowRadius
        )
        drawCircle(
            brush = glowBrush,
            radius = glowRadius,
            center = glowCenter
        )

        // 3. Draw ambient dynamic elements depending on weather code to preserve features
        when (weatherCode) {
            0 -> {
                // Clear sky - subtle stardust
                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 2f, center = Offset(w * 0.15f, h * 0.35f))
                drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 3f, center = Offset(w * 0.85f, h * 0.25f))
                drawCircle(color = Color.White.copy(alpha = 0.2f), radius = 1.5f, center = Offset(w * 0.5f, h * 0.45f))
            }
            61, 63, 65, 80, 81, 82 -> {
                // Rain - falling rain streaks styled matching cyan/blue theme
                drawLine(Color(0xFF60A5FA).copy(alpha = 0.15f), Offset(w * 0.15f, h * 0.1f), Offset(w * 0.12f, h * 0.25f), strokeWidth = 1.5f.dp.toPx())
                drawLine(Color(0xFF60A5FA).copy(alpha = 0.2f), Offset(w * 0.8f, h * 0.15f), Offset(w * 0.77f, h * 0.35f), strokeWidth = 1.5f.dp.toPx())
                drawLine(Color(0xFF60A5FA).copy(alpha = 0.1f), Offset(w * 0.4f, h * 0.4f), Offset(w * 0.37f, h * 0.58f), strokeWidth = 1.5f.dp.toPx())
                drawLine(Color(0xFF60A5FA).copy(alpha = 0.18f), Offset(w * 0.25f, h * 0.62f), Offset(w * 0.22f, h * 0.82f), strokeWidth = 1.5f.dp.toPx())
                drawLine(Color(0xFF60A5FA).copy(alpha = 0.12f), Offset(w * 0.7f, h * 0.55f), Offset(w * 0.67f, h * 0.75f), strokeWidth = 1.5f.dp.toPx())
            }
            71, 73, 75, 77, 85, 86 -> {
                // Snow - falling soft white snow blobs
                drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 5f, center = Offset(w * 0.18f, h * 0.2f))
                drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 7f, center = Offset(w * 0.82f, h * 0.35f))
                drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 6f, center = Offset(w * 0.48f, h * 0.55f))
                drawCircle(color = Color.White.copy(alpha = 0.3f), radius = 8f, center = Offset(w * 0.3f, h * 0.78f))
            }
        }
    }
}

@Composable
fun WidgetPreviewCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3135).copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("widget_preview_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF4F46E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WGT",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Home Widget",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Quick temperature check enabled",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

data class PresetCity(val name: String, val latitude: Double, val longitude: Double)
