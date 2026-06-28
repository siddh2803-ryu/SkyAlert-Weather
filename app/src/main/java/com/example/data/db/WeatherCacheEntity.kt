package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val areaName: String,
    val currentTemp: Double,
    val currentApparentTemp: Double,
    val currentHumidity: Int,
    val currentPrecipitation: Double,
    val currentWeatherCode: Int,
    val currentWindSpeed: Double,
    val hourlyJson: String,
    val dailyJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
