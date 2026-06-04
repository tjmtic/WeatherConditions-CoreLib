package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

/**
 * A single past weather observation, used by backward-looking scorers such as
 * dog-walking ("has it rained recently?"). Supplied by a historical data provider
 * in the `weather-data` module.
 *
 * @property time ISO-8601 timestamp of the observation.
 * @property precipitationAmount measured precipitation for the interval (inches).
 * @property shortForecast optional text label (e.g. "Rain", "Clear").
 */
@Serializable
data class WeatherObservation(
    val time: String,
    val precipitationAmount: Double = 0.0,
    val shortForecast: String = "",
)

/**
 * Everything an [com.abyxcz.weatherconditions.core.domain.usecase.ActivityScorer] may
 * need to score a single forecast period. Different scorers read different fields:
 *  - weather/golf/tennis: [periods] + [index] + [profile]
 *  - dog-walking: [history] (recent past) + [periods]/[index] (current conditions)
 *  - surfing (future): a marine block added here.
 *
 * Not all fields are populated for every call; a scorer's `requiredData` declares
 * what the orchestration must fetch.
 */
data class ScoringContext(
    val periods: List<WeatherPeriod>,
    val index: Int,
    val profile: PlayabilityProfile? = null,
    val history: List<WeatherObservation> = emptyList(),
)
