package com.abyxcz.weatherconditions.core.ports.outbound

interface ReverseGeocodingService {
    suspend fun getAddress(lat: Double, lon: Double): String?
}
