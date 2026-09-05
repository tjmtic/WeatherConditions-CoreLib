package com.abyxcz.weatherconditions.core.util

import com.abyxcz.weatherconditions.core.ports.outbound.Clock
class ClockHelper(private val clock: Clock) {
    fun now(): kotlin.time.Instant = clock.now()
}
