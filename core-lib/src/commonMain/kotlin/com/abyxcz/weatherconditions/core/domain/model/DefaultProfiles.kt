package com.abyxcz.weatherconditions.core.domain.model

/**
 * The built-in scoring profiles shipped to every surface (mobile app, desktop app, MCP
 * server). Single source of truth so the defaults can't drift between platforms. Users
 * may shadow these with a same-named custom profile or add new ones.
 */
object DefaultProfiles {
    val GOLF = PlayabilityProfile(
        name = "golf",
        minTemp = 60,
        maxTemp = 80,
        maxWindSpeed = 15,
        maxPrecipitation = 20,
    )

    val TENNIS = PlayabilityProfile(
        name = "tennis",
        minTemp = 55,
        maxTemp = 85,
        maxWindSpeed = 12,
        maxPrecipitation = 10,
    )

    val DEFAULT = PlayabilityProfile(
        name = "default",
        minTemp = 60,
        maxTemp = 80,
        maxWindSpeed = 15,
        maxPrecipitation = 20,
    )

    /** All built-ins, in display order. */
    val all: List<PlayabilityProfile> = listOf(GOLF, TENNIS, DEFAULT)
}
