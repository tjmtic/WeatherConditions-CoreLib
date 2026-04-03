package com.abyxcz.weatherconditions.core.domain.model

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.PlayabilitySettings
import com.abyxcz.weatherconditions.core.domain.model.VenueSettings
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import com.abyxcz.weatherconditions.core.domain.model.extractWindSpeed

class PlayabilityCalculator {
    fun calculateScore(
        period: WeatherPeriod,
        settings: PlayabilitySettings,
        venueSettings: VenueSettings? = null,
    ): Int {
        val profile =
            PlayabilityProfile(
                name = "Default",
                minTemp = venueSettings?.minTemp ?: settings.minTemp,
                maxTemp = venueSettings?.maxTemp ?: settings.maxTemp,
                maxWindSpeed = venueSettings?.maxWindSpeed ?: settings.maxWindSpeed,
                maxPrecipitation = venueSettings?.maxPrecipitation ?: settings.maxPrecipitation,
            )
        return calculateScore(profile, listOf(period), 0)
    }

    fun calculateScore(
        profile: PlayabilityProfile,
        periods: List<WeatherPeriod>,
        index: Int,
    ): Int {
        val period = periods[index]

        val temperatureScore =
            when {
                period.temperature in profile.minTemp..profile.maxTemp -> 10
                period.temperature in (profile.minTemp - 5) until profile.minTemp -> 7
                period.temperature in (profile.maxTemp + 1)..(profile.maxTemp + 5) -> 7
                period.temperature in (profile.minTemp - 10) until (profile.minTemp - 5) -> 5
                period.temperature in (profile.maxTemp + 6)..(profile.maxTemp + 10) -> 5
                else -> 2
            }

        val precipitationValue = period.probabilityOfPrecipitation?.value ?: 0.0
        val precipitationScore =
            when {
                precipitationValue == 0.0 -> 10
                precipitationValue <= profile.maxPrecipitation -> 8
                precipitationValue <= profile.maxPrecipitation + 20 -> 5
                else -> 2
            }

        val windSpeedValue = extractWindSpeed(period.windSpeed)
        val windSpeedScore =
            when {
                windSpeedValue <= profile.maxWindSpeed / 2 -> 10
                windSpeedValue <= profile.maxWindSpeed -> 8
                windSpeedValue <= profile.maxWindSpeed + 5 -> 5
                else -> 2
            }

        var conditionScore = 0
        profile.conditionWeights.forEach { (condition, weight) ->
            if (period.shortForecast.contains(condition, ignoreCase = true)) {
                conditionScore = weight
            }
        }

        var patternModifier = 0
        profile.patterns.forEach { pattern ->
            if (matchesPattern(pattern, periods, index)) {
                patternModifier += pattern.scoreModifier
            }
        }

        return (temperatureScore + precipitationScore + windSpeedScore + conditionScore + patternModifier)
            .coerceAtLeast(0)
    }

    private fun matchesPattern(
        pattern: com.abyxcz.weatherconditions.core.domain.model.WeatherPattern,
        periods: List<WeatherPeriod>,
        index: Int,
    ): Boolean {
        if (pattern.consecutiveDays > 1) {
            var matchCount = 0
            for (i in (index - pattern.consecutiveDays + 1)..index) {
                if (i >= 0 && i < periods.size) {
                    if (periods[i].shortForecast.contains(pattern.condition, ignoreCase = true)) {
                        matchCount++
                    }
                }
            }
            return matchCount >= pattern.consecutiveDays
        }
        return false
    }
}
