package com.abyxcz.weatherconditions.core.presenter

import com.abyxcz.viewpoint.location.Coordinate
import com.abyxcz.weatherconditions.core.domain.model.PlayabilitySettings
import com.abyxcz.weatherconditions.core.domain.model.Venue
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod

data class VenueUiState(
    val venues: List<Venue> = emptyList(),
    val isLoadingVenues: Boolean = false,
    val errorVenues: String? = null,
    val settings: PlayabilitySettings = PlayabilitySettings(),
    val forecastsByVenueId: Map<String, List<WeatherPeriod?>> = emptyMap(),
    val loadingForecastIds: Set<String> = emptySet(),
    val forecastErrorsByVenueId: Map<String, String> = emptyMap(),
    val filterText: String = "",
    val sortOption: SortOption = SortOption.NAME,
    val selectedVenue: Venue? = null,
    val showAddVenueDialog: Boolean = false,
    val currentLocation: Coordinate? = null,
    val isMapMode: Boolean = false,
    val reverseGeocodedAddress: String? = null,
)

enum class SortOption {
    NAME,
    DISTANCE,
    RATING,
    BEST_DAY,
    LOWEST_WIND,
    LOWEST_PRECIP,
    HIGHEST_TEMP,
    LOWEST_TEMP,
}
