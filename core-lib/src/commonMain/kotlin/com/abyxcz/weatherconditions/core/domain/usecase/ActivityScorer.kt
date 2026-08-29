package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.ActivityScore
import com.abyxcz.weatherconditions.core.domain.model.ScoringContext

/** The kinds of weather data a scorer may require the pipeline to fetch. */
enum class DataKind {
    /** Forward forecast periods (NWS / Google). */
    FORECAST,

    /** Recent past observations (e.g. last 48h precipitation). */
    HISTORICAL,

    /** Marine conditions: swell, period, tide (future surfing support). */
    MARINE,
}

/**
 * Scores a single forecast period for one activity. The proprietary scoring IP now lives
 * behind this interface so new activities (dog-walking, surfing, …) are added as new
 * implementations rather than by re-parameterizing one calculator.
 *
 * Implementations are pure and stateless: given a [ScoringContext] they return an
 * [ActivityScore]. They declare [requiredData] so the orchestration knows what to fetch.
 */
interface ActivityScorer {
    /** Stable id, e.g. "weather", "dog-walking". */
    val id: String

    /** Human-readable name for UI / tool listings. */
    val displayName: String

    /** Data the orchestration must supply in the context for this scorer to work. */
    val requiredData: Set<DataKind>

    fun score(context: ScoringContext): ActivityScore
}
