package com.abyxcz.weatherconditions.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object ClockHelper {
    fun now(): Instant = Clock.System.now()
}
