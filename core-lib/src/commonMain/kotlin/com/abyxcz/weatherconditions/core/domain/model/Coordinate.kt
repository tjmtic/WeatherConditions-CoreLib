package com.abyxcz.weatherconditions.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinate(val latitude: Double, val longitude: Double)
