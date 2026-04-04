package com.abyxcz.weatherconditions.core.ports.outbound

import kotlinx.datetime.Instant

@OptIn(kotlin.time.ExperimentalTime::class)
interface Clock {
    fun now(): Instant
}
