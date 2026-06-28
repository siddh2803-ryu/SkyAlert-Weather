package com.example.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.data.api.WeatherApiService
import com.example.data.db.WeatherCacheEntity
import com.example.data.db.WeatherDao
import com.example.data.model.CurrentForecast
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.WeatherResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

// High-fidelity domain model exposed to the UI
data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val areaName: String,
    val current: CurrentForecast,
    val hourly: HourlyForecast,
    val daily: DailyForecast,
    val cachedTime: Long
)

class WeatherRepository(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    context: Context
) {
    private val appContext = context.applicationContext
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val hourlyAdapter = moshi.adapter(HourlyForecast::class.java)
    private val dailyAdapter = moshi.adapter(DailyForecast::class.java)

    // Reactive cache updates for UI
    val cachedWeatherFlow: Flow<WeatherData?> = weatherDao.getWeatherCache().map { entity ->
        entity?.let { mapToDomain(it) }
    }

    suspend fun getCachedWeatherSync(): WeatherData? = withContext(Dispatchers.IO) {
        weatherDao.getWeatherCacheSync()?.let { mapToDomain(it) }
    }

    // Load weather from the network, geocode the area name, and cache it
    suspend fun refreshWeather(latitude: Double, longitude: Double): WeatherData = withContext(Dispatchers.IO) {
        val response = apiService.getForecast(latitude, longitude)
        val areaName = getAreaName(latitude, longitude)

        val current = response.current ?: throw Exception("Current forecast not available")
        val hourly = response.hourly ?: throw Exception("Hourly forecast not available")
        val daily = response.daily ?: throw Exception("Daily forecast not available")

        val hourlyJson = hourlyAdapter.toJson(hourly)
        val dailyJson = dailyAdapter.toJson(daily)

        val cacheEntity = WeatherCacheEntity(
            id = 0,
            latitude = latitude,
            longitude = longitude,
            areaName = areaName,
            currentTemp = current.temperature2m,
            currentApparentTemp = current.apparentTemperature,
            currentHumidity = current.relativeHumidity2m,
            currentPrecipitation = current.precipitation,
            currentWeatherCode = current.weatherCode,
            currentWindSpeed = current.windSpeed10m,
            hourlyJson = hourlyJson,
            dailyJson = dailyJson,
            timestamp = System.currentTimeMillis()
        )

        weatherDao.insertWeatherCache(cacheEntity)

        WeatherData(
            latitude = latitude,
            longitude = longitude,
            areaName = areaName,
            current = current,
            hourly = hourly,
            daily = daily,
            cachedTime = cacheEntity.timestamp
        )
    }

    private fun mapToDomain(entity: WeatherCacheEntity): WeatherData {
        val hourly = hourlyAdapter.fromJson(entity.hourlyJson) ?: HourlyForecast(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        val daily = dailyAdapter.fromJson(entity.dailyJson) ?: DailyForecast(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        return WeatherData(
            latitude = entity.latitude,
            longitude = entity.longitude,
            areaName = entity.areaName,
            current = CurrentForecast(
                time = "",
                temperature2m = entity.currentTemp,
                relativeHumidity2m = entity.currentHumidity,
                apparentTemperature = entity.currentApparentTemp,
                precipitation = entity.currentPrecipitation,
                weatherCode = entity.currentWeatherCode,
                windSpeed10m = entity.currentWindSpeed
            ),
            hourly = hourly,
            daily = daily,
            cachedTime = entity.timestamp
        )
    }

    // Try to get city name from location using Geocoder
    private fun getAreaName(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(appContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea
                val country = address.countryCode
                if (city != null) {
                    if (country != null) "$city, $country" else city
                } else {
                    String.format(Locale.US, "%.3f, %.3f", latitude, longitude)
                }
            } else {
                String.format(Locale.US, "%.3f, %.3f", latitude, longitude)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "%.3f, %.3f", latitude, longitude)
        }
    }
}
