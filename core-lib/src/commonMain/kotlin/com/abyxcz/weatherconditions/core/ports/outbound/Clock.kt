package com.abyxcz.weatherconditions.core.ports.outbound

interface Clock {
    fun now(): kotlinx.datetime.Instant
}
