package com.example.receiver

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.api.WeatherApiService
import com.example.data.db.WeatherDatabase
import com.example.data.model.WeatherCodeUtils
import com.example.data.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("WeatherAlertReceiver", "Received broadcast action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule after reboot
            scheduleHourlyAlerts(context)
        } else if (action == ACTION_CHECK_PRECIPITATION || action == ACTION_TRIGGER_TEST_NOTIFICATION) {
            if (action == ACTION_TRIGGER_TEST_NOTIFICATION) {
                // Instantly trigger a fun test notification
                triggerTestNotification(context)
                return
            }

            // Perform check in coroutine scope
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    checkAndNotifyPrecipitation(context)
                } catch (e: Exception) {
                    Log.e("WeatherAlertReceiver", "Error checking precipitation: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun checkAndNotifyPrecipitation(context: Context) {
        // 1. Get the last known location or fallback to a default location (e.g. latitude/longitude of NY or previous cached location)
        val database = WeatherDatabase.getDatabase(context)
        val apiService = WeatherApiService.create()
        val repository = WeatherRepository(apiService, database.weatherDao(), context)

        val cached = repository.getCachedWeatherSync()
        val lat: Double
        val lon: Double

        if (cached != null) {
            lat = cached.latitude
            lon = cached.longitude
        } else {
            // Default to San Francisco
            lat = 37.7749
            lon = -122.4194
        }

        // Fetch latest weather
        try {
            val weatherData = repository.refreshWeather(lat, lon)
            
            // Check the hourly precipitation for the next 4 hours
            val currentTime = System.currentTimeMillis()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            
            var precipitationDetected = false
            var alertTime = ""
            var maxProb = 0
            var precipAmount = 0.0
            var weatherDesc = ""

            val times = weatherData.hourly.time
            val probs = weatherData.hourly.precipitationProbability
            val amounts = weatherData.hourly.precipitation
            val codes = weatherData.hourly.weatherCode

            // Scan next 6 hours
            for (i in times.indices) {
                try {
                    val date = format.parse(times[i])
                    if (date != null && date.time > currentTime && date.time < currentTime + 6 * 3600 * 1000) {
                        if (probs[i] >= 30 || amounts[i] > 0.1) {
                            precipitationDetected = true
                            if (probs[i] > maxProb) {
                                maxProb = probs[i]
                                precipAmount = amounts[i]
                                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                alertTime = timeFormat.format(date)
                                weatherDesc = WeatherCodeUtils.getDescription(codes[i])
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parsing error for specific line
                }
            }

            if (precipitationDetected) {
                sendPrecipitationNotification(
                    context,
                    title = "Precipitation Warning - $weatherDesc",
                    text = "Expected at $alertTime with $maxProb% chance ($precipAmount mm). Grab your umbrella!",
                    notificationId = PRECIP_NOTIFICATION_ID
                )
            }
        } catch (e: Exception) {
            Log.e("WeatherAlertReceiver", "Error in weather fetch: ${e.message}")
        }
    }

    private fun triggerTestNotification(context: Context) {
        sendPrecipitationNotification(
            context,
            title = "SkyAlert: Rain Alert Active",
            text = "Light rain expected around 3:30 PM (65% chance, 1.2 mm). This is a test notification verifying your live precipitation alerts!",
            notificationId = TEST_NOTIFICATION_ID
        )
    }

    @SuppressLint("MissingPermission")
    private fun sendPrecipitationNotification(context: Context, title: String, text: String, notificationId: Int) {
        val channelId = "weather_precipitation_alerts"
        createNotificationChannel(context, channelId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard system info icon fallback
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    manager.notify(notificationId, builder.build())
                } else {
                    Log.w("WeatherAlertReceiver", "POST_NOTIFICATIONS permission not granted.")
                }
            } else {
                manager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            Log.e("WeatherAlertReceiver", "Failed to show notification: ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_CHECK_PRECIPITATION = "com.example.weather.ACTION_CHECK_PRECIPITATION"
        const val ACTION_TRIGGER_TEST_NOTIFICATION = "com.example.weather.ACTION_TRIGGER_TEST_NOTIFICATION"
        private const val PRECIP_NOTIFICATION_ID = 1001
        private const val TEST_NOTIFICATION_ID = 1002

        fun scheduleHourlyAlerts(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WeatherAlertReceiver::class.java).apply {
                action = ACTION_CHECK_PRECIPITATION
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Trigger every hour
            val interval = AlarmManager.INTERVAL_HOUR
            val triggerAt = Calendar.getInstance().apply {
                add(Calendar.HOUR, 1)
            }.timeInMillis

            try {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    interval,
                    pendingIntent
                )
                Log.d("WeatherAlertReceiver", "Hourly precipitation checks scheduled successfully.")
            } catch (e: SecurityException) {
                Log.e("WeatherAlertReceiver", "Could not schedule alarm: ${e.message}")
            }
        }
    }
}
