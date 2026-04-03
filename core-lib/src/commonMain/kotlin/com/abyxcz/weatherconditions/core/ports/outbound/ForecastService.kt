package com.abyxcz.weatherconditions.core.ports.outbound

import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import kotlinx.serialization.json.JsonObject

interface ForecastService {
    suspend fun getForecastForLocation(
        lat: Double,
        lon: Double,
        venueId: Long? = null,
    ): List<WeatherPeriod>

    suspend fun getPoints(
        lat: Double,
        lon: Double,
    ): JsonObject

    suspend fun getForecastPoints(
        caw: String,
        gridX: String,
        gridY: String,
    ): JsonObject
}
