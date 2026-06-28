package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.CurrentForecast
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.repository.WeatherData
import com.example.ui.screens.WeatherHeroSection
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun weather_hero_screenshot() {
    val mockData = WeatherData(
        latitude = 37.7749,
        longitude = -122.4194,
        areaName = "San Francisco, US",
        current = CurrentForecast(
            time = "2026-06-24T12:00",
            temperature2m = 22.5,
            relativeHumidity2m = 65,
            apparentTemperature = 21.8,
            precipitation = 0.0,
            weatherCode = 1,
            windSpeed10m = 12.0
        ),
        hourly = HourlyForecast(
            time = listOf("2026-06-24T12:00", "2026-06-24T13:00"),
            temperature2m = listOf(22.5, 23.0),
            precipitationProbability = listOf(10, 20),
            precipitation = listOf(0.0, 0.0),
            weatherCode = listOf(1, 1)
        ),
        daily = DailyForecast(
            time = listOf("2026-06-24"),
            weatherCode = listOf(1),
            temperature2mMax = listOf(24.0),
            temperature2mMin = listOf(15.0),
            precipitationProbabilityMax = listOf(10)
        ),
        cachedTime = 1782300000000L // Static timestamp for deterministic testing
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        WeatherHeroSection(data = mockData, isCelsius = true)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
