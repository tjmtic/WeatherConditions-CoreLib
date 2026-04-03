package com.abyxcz.weatherconditions.core.domain.model

data class VenueSettings(
    val id: Long? = null,
    val venueId: Long,
    val minTemp: Int? = null,
    val maxTemp: Int? = null,
    val maxWindSpeed: Int? = null,
    val maxPrecipitation: Int? = null,
    val profileId: Long? = null,
)
