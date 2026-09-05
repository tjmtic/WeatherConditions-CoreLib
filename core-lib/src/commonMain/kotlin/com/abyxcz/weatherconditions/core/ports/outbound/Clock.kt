package com.abyxcz.weatherconditions.core.ports.outbound

interface Clock {
    fun now(): kotlin.time.Instant
}
