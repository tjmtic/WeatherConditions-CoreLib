package com.abyxcz.weatherconditions.core.ports.outbound

import com.abyxcz.viewpoint.location.Coordinate
import kotlinx.coroutines.flow.StateFlow

interface LocationService {
    val currentLocation: StateFlow<Coordinate?>
    fun startLocationUpdates()
    fun stopLocationUpdates()
}
