package com.abyxcz.weatherconditions.core.ports.outbound

import com.abyxcz.weatherconditions.core.domain.model.Venue
import kotlinx.coroutines.flow.Flow

interface VenueRepository {
    suspend fun getVenues(): List<Venue>
    suspend fun addVenue(venue: Venue)
    suspend fun updateVenue(venue: Venue)
    suspend fun removeVenue(venueId: Long)
    suspend fun syncVenues(venues: List<Venue>)
    fun getVenuesFlow(): Flow<List<Venue>>
}
