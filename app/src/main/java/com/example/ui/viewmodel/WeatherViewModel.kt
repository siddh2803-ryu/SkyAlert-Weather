package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.WeatherApiService
import com.example.data.db.WeatherDatabase
import com.example.data.repository.WeatherData
import com.example.data.repository.WeatherRepository
import com.example.receiver.WeatherAlertReceiver
import com.example.widget.WeatherWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    object Idle : WeatherUiState
    object Loading : WeatherUiState
    data class Success(val data: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Observe DB cache reactively
    val cachedWeather: StateFlow<WeatherData?> = repository.cachedWeatherFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun fetchWeather(latitude: Double, longitude: Double, context: Context) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                Log.d("WeatherViewModel", "Fetching weather for lat=$latitude, lon=$longitude")
                val weather = repository.refreshWeather(latitude, longitude)
                _uiState.value = WeatherUiState.Success(weather)
                
                // Instantly sync the widget
                WeatherWidgetProvider.triggerUpdate(context)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather", e)
                _uiState.value = WeatherUiState.Error(e.message ?: "Failed to load weather forecast")
            }
        }
    }

    fun triggerTestNotification(context: Context) {
        val intent = Intent(context, WeatherAlertReceiver::class.java).apply {
            action = WeatherAlertReceiver.ACTION_TRIGGER_TEST_NOTIFICATION
        }
        context.sendBroadcast(intent)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = WeatherDatabase.getDatabase(context)
            val apiService = WeatherApiService.create()
            val repository = WeatherRepository(apiService, database.weatherDao(), context)
            return WeatherViewModel(repository) as T
        }
    }
}
