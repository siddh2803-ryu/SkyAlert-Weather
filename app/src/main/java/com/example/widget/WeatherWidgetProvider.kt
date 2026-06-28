package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.WeatherDatabase
import com.example.data.model.WeatherCodeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("WeatherWidgetProvider", "onUpdate triggered")
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        // Set click listener to open MainActivity
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // Load cached weather from database in IO thread
        val db = WeatherDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cache = db.weatherDao().getWeatherCacheSync()
                if (cache != null) {
                    val tempStr = "${cache.currentTemp.toInt()}°"
                    val desc = WeatherCodeUtils.getDescription(cache.currentWeatherCode)
                    
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val timeStr = "Checked: ${timeFormat.format(Date(cache.timestamp))}"

                    views.setTextViewText(R.id.widget_temp, tempStr)
                    views.setTextViewText(R.id.widget_location, cache.areaName)
                    views.setTextViewText(R.id.widget_desc, desc)
                    views.setTextViewText(R.id.widget_time, timeStr)
                } else {
                    views.setTextViewText(R.id.widget_location, "Tap to load weather")
                    views.setTextViewText(R.id.widget_desc, "No cache found")
                }
            } catch (e: Exception) {
                Log.e("WeatherWidgetProvider", "Error reading widget cache: ${e.message}")
            } finally {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        fun triggerUpdate(context: Context) {
            val intent = Intent(context, WeatherWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, WeatherWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
