package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayabilityScoreBreakdown(
    val temperatureScore: Int,
    val precipitationScore: Int,
    val windSpeedScore: Int,
    val conditionScore: Int,
    val patternModifier: Int,
    val lookbackPenalty: Int,
    val totalScore: Int
)
