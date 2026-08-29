package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.ActivityScore
import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.ScoreFactor
import com.abyxcz.weatherconditions.core.domain.model.ScoringContext

/**
 * Adapts the existing forward-forecast [PlayabilityCalculator] to the [ActivityScorer]
 * interface. Serves golf, tennis, default, and any user-defined [PlayabilityProfile] —
 * the algorithm is identical; only the profile thresholds differ.
 *
 * This preserves the original scoring engine (and its tests) unchanged while exposing it
 * through the new registry alongside heterogeneous scorers like dog-walking.
 */
class WeatherConditionScorer(
    private val calculator: PlayabilityCalculator = PlayabilityCalculator(),
) : ActivityScorer {
    override val id: String = "weather"
    override val displayName: String = "Weather Conditions"
    override val requiredData: Set<DataKind> = setOf(DataKind.FORECAST)

    override fun score(context: ScoringContext): ActivityScore {
        val profile = context.profile ?: PlayabilityProfile(name = "default")
        val b = calculator.calculateScore(profile, context.periods, context.index)
        return ActivityScore(
            activity = profile.name,
            total = b.totalScore,
            maxScore = 40,
            factors = listOf(
                ScoreFactor("temperature", b.temperatureScore),
                ScoreFactor("precipitation", b.precipitationScore),
                ScoreFactor("wind", b.windSpeedScore),
                ScoreFactor("conditions", b.conditionScore),
                ScoreFactor("pattern", b.patternModifier),
                ScoreFactor("overnightRain", b.lookbackPenalty),
            ),
        )
    }
}
