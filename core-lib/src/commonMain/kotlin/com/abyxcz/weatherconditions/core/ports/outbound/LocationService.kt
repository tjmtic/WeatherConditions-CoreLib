package com.abyxcz.weatherconditions.core.ports.outbound

import com.abyxcz.weatherconditions.core.domain.model.Coordinate
import kotlinx.coroutines.flow.StateFlow

interface LocationService {
    val currentLocation: StateFlow<Coordinate?>
    fun startLocationUpdates()
    fun stopLocationUpdates()
}
