package com.abyxcz.weatherconditions.core.domain.model

data class Venue(
    val id: Long?,
    val title: String,
    val lat: Double,
    val lon: Double,
    val websiteUrl: String? = null,
    val forecast: List<WeatherPeriod?> = MutableList(14) { null },
    val settings: VenueSettings? = null,
) {
    val optimalDayNumber: Int?
        get() = forecast.filterNotNull().maxByOrNull { it.playabilityScore ?: 0 }?.number
}
