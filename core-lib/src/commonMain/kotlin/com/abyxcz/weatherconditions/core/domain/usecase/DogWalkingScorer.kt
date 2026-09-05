package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.ActivityScore
import com.abyxcz.weatherconditions.core.domain.model.ScoreFactor
import com.abyxcz.weatherconditions.core.domain.model.ScoringContext
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import kotlin.time.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Scores how good conditions are for walking a dog in an area. Unlike the forward-looking
 * golf/tennis scorer, this is primarily **backward-looking**: a walk is pleasant when the
 * ground is dry, i.e. it hasn't rained measurably in the recent past.
 *
 * Factors (max 40):
 *  - `recentRain` (0..25): hours since the last measurable rain in the trailing window.
 *  - `currentPrecip` (0..10): whether it's wet right now / this period.
 *  - `temperature` (0..5): comfort for a walk.
 *
 * Requires [DataKind.HISTORICAL] so the orchestration supplies recent observations; it also
 * folds in any earlier forecast periods that fall inside the look-back window.
 */
class DogWalkingScorer(
    /** How many hours back counts as "recent" for ground wetness. */
    private val lookbackHours: Int = 48,
    /** Precipitation (inches) at or above which an interval counts as "wet". */
    private val wetThresholdInches: Double = 0.01,
) : ActivityScorer {
    override val id: String = "dog-walking"
    override val displayName: String = "Dog Walking"
    override val requiredData: Set<DataKind> = setOf(DataKind.HISTORICAL, DataKind.FORECAST)

    private data class PrecipEvent(val instant: Instant, val wet: Boolean)

    override fun score(context: ScoringContext): ActivityScore {
        val period = context.periods.getOrNull(context.index)
            ?: return ActivityScore(id, 0, summary = "No forecast period to score.")
        val ref = parseInstant(period.startTime)
            ?: return ActivityScore(id, 0, summary = "Unparseable period time.")

        val window = lookbackHours.hours
        val events = buildList {
            context.history.forEach { obs ->
                parseInstant(obs.time)?.let { add(PrecipEvent(it, obs.precipitationAmount >= wetThresholdInches)) }
            }
            // Earlier forecast periods that fall within the look-back window also count.
            context.periods.take(context.index).forEach { p ->
                parseInstant(p.startTime)?.let { add(PrecipEvent(it, p.isWet(wetThresholdInches))) }
            }
        }.filter { it.instant <= ref && (ref - it.instant) <= window }

        val hadData = events.isNotEmpty()
        val lastWet = events.filter { it.wet }.maxByOrNull { it.instant }?.instant
        val hoursSinceRain = lastWet?.let { (ref - it).inWholeHours }

        val recentRain = when {
            !hadData -> ScoreFactor("recentRain", 18, "No recent observations available; assuming likely dry.")
            hoursSinceRain == null -> ScoreFactor("recentRain", 25, "No measurable rain in the last $lookbackHours h — ground should be dry.")
            hoursSinceRain >= 36 -> ScoreFactor("recentRain", 20, "Last rain ~${hoursSinceRain}h ago; mostly dried out.")
            hoursSinceRain >= 24 -> ScoreFactor("recentRain", 14, "Rain ~${hoursSinceRain}h ago; ground may be damp.")
            hoursSinceRain >= 12 -> ScoreFactor("recentRain", 8, "Rain ~${hoursSinceRain}h ago; likely muddy.")
            else -> ScoreFactor("recentRain", 2, "Rain within the last ${hoursSinceRain}h; ground is wet.")
        }

        val prob = period.probabilityOfPrecipitation?.value ?: 0.0
        val amt = period.precipitationAmount?.value ?: 0.0
        val currentPrecip = when {
            amt > 0.1 || prob >= 60 -> ScoreFactor("currentPrecip", 2, "Rain likely during the walk.")
            amt > wetThresholdInches || prob >= 30 -> ScoreFactor("currentPrecip", 6, "Some chance of rain during the walk.")
            else -> ScoreFactor("currentPrecip", 10, "Dry during the walk.")
        }

        val t = period.temperature
        val temperature = when {
            t in 40..75 -> ScoreFactor("temperature", 5, "Comfortable for a walk.")
            t in 32..85 -> ScoreFactor("temperature", 3, "A bit warm/cool but workable.")
            else -> ScoreFactor("temperature", 1, "Uncomfortable temperature for paws.")
        }

        val factors = listOf(recentRain, currentPrecip, temperature)
        val total = factors.sumOf { it.score }
        return ActivityScore(
            activity = id,
            total = total,
            maxScore = 40,
            factors = factors,
            summary = recentRain.detail,
        )
    }

    private fun parseInstant(iso: String): Instant? = runCatching { Instant.parse(iso) }.getOrNull()

    private fun WeatherPeriod.isWet(threshold: Double): Boolean {
        val amt = precipitationAmount?.value ?: 0.0
        val prob = probabilityOfPrecipitation?.value ?: 0.0
        return amt >= threshold || prob >= 60.0
    }
}
