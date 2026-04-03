package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayabilityProfile(
    val id: Long? = null,
    val name: String,
    val minTemp: Int = 60,
    val maxTemp: Int = 80,
    val maxWindSpeed: Int = 15,
    val maxPrecipitation: Int = 20,
    val conditionWeights: Map<String, Int> =
        mapOf(
            "Clear" to 10,
            "Sunny" to 10,
            "Partly Cloudy" to 8,
            "Cloudy" to 6,
            "Rain" to 0,
            "Snow" to 0,
        ),
    val patterns: List<WeatherPattern> = emptyList(),
)

@Serializable
data class WeatherPattern(
    val id: Long? = null,
    val condition: String,
    val consecutiveDays: Int,
    val scoreModifier: Int,
    val description: String,
)
