package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.ports.outbound.NotificationService
import com.abyxcz.weatherconditions.core.ports.outbound.SettingsRepository
import com.abyxcz.weatherconditions.core.ports.outbound.VenueRepository
import com.abyxcz.weatherconditions.core.domain.usecase.PlayabilityCalculator
import com.abyxcz.weatherconditions.core.ports.outbound.ForecastService
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import com.abyxcz.weatherconditions.core.ports.outbound.Logger
import kotlinx.coroutines.flow.first

class WeatherAlertManager(
    private val forecastService: ForecastService,
    private val venueRepository: VenueRepository,
    private val playabilityCalculator: PlayabilityCalculator,
    private val settingsRepository: SettingsRepository,
    private val notificationService: NotificationService,
    private val logger: Logger,
) {
    suspend fun checkWeatherAndNotify() {
        val venues = venueRepository.getVenues()
        val settings = settingsRepository.getSettings().first()
        val threshold = settings.notificationThreshold

        venues.forEach { venue ->
            try {
                val periods: List<WeatherPeriod> =
                    forecastService.getForecastForLocation(
                        venue.lat,
                        venue.lon,
                        venue.id,
                    )

                val highScores = mutableListOf<Int>()
                periods.take(4).forEach { period: WeatherPeriod ->
                    val score = playabilityCalculator.calculateScore(period, settings, venue.settings)
                    if (score >= threshold) {
                        highScores.add(score)
                    }
                }

                if (highScores.isNotEmpty()) {
                    val maxScore = highScores.maxOrNull() ?: 0
                    notificationService.showNotification(
                        title = "Good weather at ${venue.title}!",
                        message = "Score reached $maxScore. Time to go out!",
                    )
                }
            } catch (e: Exception) {
                logger.e("WeatherAlertManager", "Failed to check weather for ${venue.title}", e)
            }
        }
    }
}
