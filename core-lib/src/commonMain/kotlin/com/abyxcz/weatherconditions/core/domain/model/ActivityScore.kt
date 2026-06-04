package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

/**
 * One named contribution to an [ActivityScore]. Replaces the fixed-field
 * [PlayabilityScoreBreakdown] for activities whose factors differ (e.g. surfing's
 * swell vs golf's temperature), so each scorer reports its own breakdown.
 */
@Serializable
data class ScoreFactor(
    val name: String,
    val score: Int,
    val detail: String = "",
)

/**
 * The generalized result of any [com.abyxcz.weatherconditions.core.domain.usecase.ActivityScorer].
 *
 * @property activity the scorer/profile id this score is for (e.g. "golf", "dog-walking").
 * @property total the headline score.
 * @property maxScore the nominal maximum so consumers can compare across activities.
 * @property factors the per-factor breakdown.
 * @property summary a short human-readable verdict.
 */
@Serializable
data class ActivityScore(
    val activity: String,
    val total: Int,
    val maxScore: Int = 40,
    val factors: List<ScoreFactor> = emptyList(),
    val summary: String = "",
)
