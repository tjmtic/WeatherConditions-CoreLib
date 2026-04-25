package com.abyxcz.weatherconditions.core.presenter

import com.abyxcz.viewpoint.location.Coordinate
import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.PlayabilitySettings
import com.abyxcz.weatherconditions.core.domain.model.Venue
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import com.abyxcz.weatherconditions.core.domain.usecase.PlayabilityCalculator
import com.abyxcz.weatherconditions.core.ports.outbound.ForecastService
import com.abyxcz.weatherconditions.core.ports.outbound.LocationService
import com.abyxcz.weatherconditions.core.ports.outbound.Logger
import com.abyxcz.weatherconditions.core.ports.outbound.SettingsRepository
import com.abyxcz.weatherconditions.core.ports.outbound.ReverseGeocodingService
import com.abyxcz.weatherconditions.core.ports.outbound.VenueRepository
import com.abyxcz.weatherconditions.core.util.ClockHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VenuePresenter(
    private val venueRepository: VenueRepository,
    private val forecastService: ForecastService,
    private val playabilityCalculator: PlayabilityCalculator,
    private val settingsRepository: SettingsRepository,
    private val locationService: LocationService,
    private val reverseGeocodingService: ReverseGeocodingService,
    private val logger: Logger,
    private val clockHelper: ClockHelper,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    private val _state = MutableStateFlow(VenueUiState())
    val state: StateFlow<VenueUiState> = _state

    private var currentSettings = PlayabilitySettings()
    private var allProfiles = emptyList<PlayabilityProfile>()

    init {
        scope.launch {
            settingsRepository.getSettings().collect { settings ->
                currentSettings = settings
                _state.update { it.copy(settings = settings) }
                if (_state.value.venues.isNotEmpty()) {
                    recalculateScores()
                }
            }
        }
        scope.launch {
            settingsRepository.getAllProfiles().collect { profiles ->
                allProfiles = profiles
                if (_state.value.venues.isNotEmpty()) {
                    recalculateScores()
                }
            }
        }
        scope.launch {
            locationService.currentLocation.collect { location ->
                _state.update { it.copy(currentLocation = location) }
            }
        }
    }

    fun loadVenuesWithForecasts() {
        scope.launch {
            _state.update { it.copy(isLoadingVenues = true, errorVenues = null) }

            try {
                val venues = venueRepository.getVenues()
                _state.update {
                    it.copy(
                        venues = venues,
                        isLoadingVenues = false,
                        errorVenues = null,
                        settings = currentSettings,
                    )
                }

                venues.forEach { venue ->
                    loadForecastForVenue(venue)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingVenues = false,
                        errorVenues = e.message ?: "Failed to load venues",
                    )
                }
            }
        }
    }

    private fun loadForecastForVenue(venue: Venue) {
        val venueId = venue.id ?: return
        scope.launch {
            _state.update { s ->
                s.copy(loadingForecastIds = s.loadingForecastIds + venueId.toString())
            }

            try {
                val forecast = forecastService.getForecastForLocation(venue.lat, venue.lon, venue.id)
                val scoredForecast = applyScoresToForecast(venue, forecast)

                _state.update { s ->
                    s.copy(
                        venues =
                            s.venues.map { v ->
                                if (v.id == venueId) v.copy(forecast = scoredForecast) else v
                            },
                        loadingForecastIds = s.loadingForecastIds - venueId.toString(),
                        forecastsByVenueId = s.forecastsByVenueId + (venueId.toString() to scoredForecast),
                        forecastErrorsByVenueId = s.forecastErrorsByVenueId - venueId.toString(),
                    )
                }
            } catch (e: Exception) {
                _state.update { s ->
                    s.copy(
                        loadingForecastIds = s.loadingForecastIds - venueId.toString(),
                        forecastErrorsByVenueId =
                            s.forecastErrorsByVenueId + (venueId.toString() to (e.message ?: "Forecast error")),
                    )
                }
            }
        }
    }

    fun showAddVenueDialog() {
        locationService.startLocationUpdates()
        _state.update { it.copy(showAddVenueDialog = true, selectedVenue = null) }
    }

    fun showEditVenueDialog(venue: Venue) {
        _state.update { it.copy(showAddVenueDialog = true, selectedVenue = venue) }
    }

    fun hideAddVenueDialog() {
        locationService.stopLocationUpdates()
        _state.update { it.copy(showAddVenueDialog = false, selectedVenue = null, reverseGeocodedAddress = null) }
    }

    fun toggleMapMode() {
        _state.update { it.copy(isMapMode = !it.isMapMode) }
    }

    fun onVenueFocused(venue: Venue?) {
        _state.update { it.copy(focusedVenue = venue) }
    }

    fun onCoordinateSelected(lat: Double, lon: Double) {
        scope.launch {
            try {
                val address = reverseGeocodingService.getAddress(lat, lon)
                _state.update { it.copy(reverseGeocodedAddress = address) }
            } catch (e: Exception) {
                logger.e("VenuePresenter", "Failed to reverse geocode: ${e.message}")
            }
        }
    }

    fun onFilterChange(text: String) {
        _state.update { it.copy(filterText = text) }
    }

    fun onSortChange(option: SortOption) {
        _state.update { it.copy(sortOption = option) }
        applySort(option)
    }

    fun addVenue(venue: Venue) {
        scope.launch {
            try {
                venueRepository.addVenue(venue)
                loadVenuesWithForecasts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateVenue(venue: Venue) {
        scope.launch {
            try {
                venueRepository.updateVenue(venue)
                loadVenuesWithForecasts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeVenue(venue: Venue) {
        scope.launch {
            try {
                venue.id?.let {
                    venueRepository.removeVenue(it)
                    _state.update { s ->
                        s.copy(venues = s.venues.filter { v -> v.id != it })
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun recalculateScores() {
        _state.update { s ->
            val updatedVenues =
                s.venues.map { venue ->
                    venue.copy(forecast = applyScoresToForecast(venue, venue.forecast))
                }
            s.copy(venues = updatedVenues)
        }
    }

    private fun applyScoresToForecast(
        venue: Venue,
        forecast: List<WeatherPeriod?>,
    ): List<WeatherPeriod?> {
        val periods = forecast.filterNotNull()
        return forecast.map { period ->
            if (period != null) {
                val periodIndex = periods.indexOf(period)

                val venueProfile = venue.settings?.profileId?.let { id -> allProfiles.find { it.id == id } }
                val globalProfile = currentSettings.activeProfileId?.let { id -> allProfiles.find { it.id == id } }
                val selectedProfile = venueProfile ?: globalProfile

                val score =
                    if (selectedProfile != null && periodIndex != -1) {
                        playabilityCalculator.calculateScore(selectedProfile, periods, periodIndex)
                    } else {
                        playabilityCalculator.calculateScore(period, currentSettings, venue.settings)
                    }
                period.copy(playabilityScore = score)
            } else {
                null
            }
        }
    }

    private fun applySort(option: SortOption) {
        _state.update { s ->
            val sorted =
                when (option) {
                    SortOption.NAME -> s.venues.sortedBy { it.title }
                    SortOption.BEST_DAY -> s.venues.sortedByDescending { it.optimalDayNumber }
                    SortOption.HIGHEST_TEMP ->
                        s.venues.sortedByDescending { v ->
                            v.forecast.firstOrNull()?.temperature ?: Int.MIN_VALUE
                        }
                    SortOption.LOWEST_TEMP ->
                        s.venues.sortedBy { v ->
                            v.forecast.firstOrNull()?.temperature ?: Int.MAX_VALUE
                        }
                    else -> s.venues
                }
            s.copy(venues = sorted)
        }
    }
}
