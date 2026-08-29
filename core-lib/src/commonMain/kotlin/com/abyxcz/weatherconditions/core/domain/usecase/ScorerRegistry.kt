package com.abyxcz.weatherconditions.core.domain.usecase

/**
 * The single source of truth for which activity scorers exist. Both the app's activity
 * picker and the MCP server's tool surface should enumerate this registry, so adding a
 * scorer surfaces it everywhere with no per-consumer wiring.
 */
class ScorerRegistry(
    scorers: List<ActivityScorer> = defaultScorers(),
) {
    private val byId: Map<String, ActivityScorer> = scorers.associateBy { it.id.lowercase() }

    /** All registered scorers, in registration order. */
    val all: List<ActivityScorer> = scorers

    /** Resolve a scorer by id (case-insensitive). */
    fun byId(id: String?): ActivityScorer? = id?.lowercase()?.let { byId[it] }

    /** Ids of every registered scorer. */
    fun ids(): List<String> = all.map { it.id }

    companion object {
        fun defaultScorers(): List<ActivityScorer> = listOf(
            WeatherConditionScorer(),
            DogWalkingScorer(),
        )
    }
}
