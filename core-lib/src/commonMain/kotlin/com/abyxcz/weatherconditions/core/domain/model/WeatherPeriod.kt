package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherValue(
    val unitCode: String? = null,
    val value: Double? = null,
)

@Serializable
data class WeatherPeriod(
    val number: Int = 0,
    val name: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val isDaytime: Boolean = true,
    val temperature: Int = 0,
    val temperatureUnit: String = "F",
    val temperatureTrend: String? = null,
    val probabilityOfPrecipitation: WeatherValue? = null,
    val relativeHumidity: WeatherValue? = null,
    val windSpeed: String = "",
    val windDirection: String = "",
    val icon: String = "",
    val shortForecast: String = "",
    val detailedForecast: String = "",
    val playabilityScore: Int? = null,
) {
    fun getEffectiveScore(): Int = playabilityScore ?: 0
}

fun extractWindSpeed(windSpeed: String): Int {
    val regex = Regex("(\\d+)")
    val matches = regex.findAll(windSpeed)
    val speeds = matches.map { it.value.toInt() }.toList()
    return if (speeds.isNotEmpty()) speeds.maxOrNull() ?: 0 else 0
}
