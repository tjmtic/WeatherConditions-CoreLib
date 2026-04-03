package com.abyxcz.weatherconditions.core.domain.model

data class PlayabilitySettings(
    val minTemp: Int = 60,
    val maxTemp: Int = 80,
    val maxWindSpeed: Int = 15,
    val maxPrecipitation: Int = 20,
    val themeName: String = "Midnight",
    val accentColor: Int = -1490226,
    val notificationThreshold: Int = 30,
    val activeProfileId: Long? = null,
)
