package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = 0")
    fun getWeatherCache(): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE id = 0")
    suspend fun getWeatherCacheSync(): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clearCache()
}
