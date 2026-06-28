package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @Json(name = "timezone_abbreviation") val timezoneAbbreviation: String,
    val elevation: Double,
    @Json(name = "current_units") val currentUnits: CurrentUnits?,
    val current: CurrentForecast?,
    @Json(name = "hourly_units") val hourlyUnits: HourlyUnits?,
    val hourly: HourlyForecast?,
    @Json(name = "daily_units") val dailyUnits: DailyUnits?,
    val daily: DailyForecast?
)

@JsonClass(generateAdapter = true)
data class CurrentUnits(
    @Json(name = "time") val time: String?,
    @Json(name = "temperature_2m") val temperature2m: String?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: String?,
    @Json(name = "apparent_temperature") val apparentTemperature: String?,
    @Json(name = "precipitation") val precipitation: String?,
    @Json(name = "weather_code") val weatherCode: String?,
    @Json(name = "wind_speed_10m") val windSpeed10m: String?
)

@JsonClass(generateAdapter = true)
data class CurrentForecast(
    val time: String,
    @Json(name = "temperature_2m") val temperature2m: Double,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    val precipitation: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double
)

@JsonClass(generateAdapter = true)
data class HourlyUnits(
    val time: String?,
    @Json(name = "temperature_2m") val temperature2m: String?,
    @Json(name = "precipitation_probability") val precipitationProbability: String?,
    @Json(name = "precipitation") val precipitation: String?,
    @Json(name = "weather_code") val weatherCode: String?
)

@JsonClass(generateAdapter = true)
data class HourlyForecast(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature2m: List<Double>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>,
    val precipitation: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyUnits(
    val time: String?,
    @Json(name = "weather_code") val weatherCode: String?,
    @Json(name = "temperature_2m_max") val temperature2mMax: String?,
    @Json(name = "temperature_2m_min") val temperature2mMin: String?,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: String?
)

@JsonClass(generateAdapter = true)
data class DailyForecast(
    val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>
)

// Helper mapping function to get a weather description based on WMO codes
object WeatherCodeUtils {
    fun getDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Mainly Clear"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rainy"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snowy"
            77 -> "Snow Grains"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Unknown"
        }
    }
}
