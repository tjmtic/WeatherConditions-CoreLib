package com.abyxcz.weatherconditions.core.util

import com.abyxcz.weatherconditions.core.ports.outbound.Clock
import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
class ClockHelper(private val clock: Clock) {
    fun now(): Instant = clock.now()
}
