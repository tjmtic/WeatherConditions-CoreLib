package com.abyxcz.weatherconditions.core.presenter

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.PlayabilitySettings
import com.abyxcz.weatherconditions.core.domain.model.Venue
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import com.abyxcz.weatherconditions.core.domain.usecase.PlayabilityCalculator
import com.abyxcz.weatherconditions.core.ports.outbound.*
import com.abyxcz.weatherconditions.core.util.ClockHelper
import com.abyxcz.viewpoint.location.Coordinate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VenuePresenterTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var venueRepository: FakeVenueRepository
    private lateinit var forecastService: FakeForecastService
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var locationService: FakeLocationService
    private lateinit var reverseGeocodingService: FakeReverseGeocodingService
    private lateinit var playabilityCalculator: PlayabilityCalculator
    private lateinit var presenter: VenuePresenter

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        venueRepository = FakeVenueRepository()
        forecastService = FakeForecastService()
        settingsRepository = FakeSettingsRepository()
        locationService = FakeLocationService()
        reverseGeocodingService = FakeReverseGeocodingService()
        playabilityCalculator = PlayabilityCalculator()
        presenter =
            VenuePresenter(
                venueRepository = venueRepository,
                forecastService = forecastService,
                playabilityCalculator = playabilityCalculator,
                settingsRepository = settingsRepository,
                locationService = locationService,
                reverseGeocodingService = reverseGeocodingService,
                logger = FakeLogger(),
                clockHelper = ClockHelper(FakeClock()),
                scope = testScope,
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadVenuesWithForecasts loads venues and forecasts`() =
        runTest(testDispatcher) {
            // Given
            val venues = listOf(Venue(1, "Test Venue", 0.0, 0.0))
            venueRepository.venues = venues
            val forecast = listOf(WeatherPeriod(number = 1, name = "Today", temperature = 25, temperatureUnit = "C"))
            forecastService.forecast = forecast

            // When
            presenter.loadVenuesWithForecasts()
            advanceUntilIdle()

            // Then
            val state = presenter.state.value
            assertEquals(venues.size, state.venues.size)
            assertEquals("Test Venue", state.venues[0].title)
            // Note: VenuePresenter might have updated the score, so we check content except for score or verify score
            assertEquals(forecast[0].name, state.venues[0].forecast[0]?.name)
            assertEquals(false, state.isLoadingVenues)
        }

    @Test
    fun `filter venues updates filterText`() =
        runTest(testDispatcher) {
            presenter.onFilterChange("query")
            assertEquals("query", presenter.state.value.filterText)
        }

    @Test
    fun `sorting updates sortOption and sorts list`() =
        runTest(testDispatcher) {
            // Given
            val v1 = Venue(1, "A Venue", 0.0, 0.0)
            val v2 = Venue(2, "B Venue", 0.0, 0.0)
            venueRepository.venues = listOf(v2, v1) // Start unsorted relative to name

            presenter.loadVenuesWithForecasts()
            advanceUntilIdle()

            // When
            presenter.onSortChange(SortOption.NAME)
            advanceUntilIdle() // Should be instant but good practice

            // Then
            assertEquals(SortOption.NAME, presenter.state.value.sortOption)
            assertEquals("A Venue", presenter.state.value.venues[0].title)
            assertEquals("B Venue", presenter.state.value.venues[1].title)
        }

    @Test
    fun `addVenue adds venue and reloads`() =
        runTest(testDispatcher) {
            // Given
            val newVenue = Venue(3, "New Venue", 10.0, 10.0)

            // When
            presenter.addVenue(newVenue)
            advanceUntilIdle()

            // Then
            assertEquals(1, presenter.state.value.venues.size)
            assertEquals("New Venue", presenter.state.value.venues[0].title)
        }

    @Test
    fun `removeVenue removes venue from list`() =
        runTest(testDispatcher) {
            // Given
            val venue = Venue(1, "Delete Me", 0.0, 0.0)
            venueRepository.venues = listOf(venue)
            presenter.loadVenuesWithForecasts()
            advanceUntilIdle()

            // When
            presenter.removeVenue(venue)
            advanceUntilIdle()

            // Then
            assertTrue(presenter.state.value.venues.isEmpty())
        }

    @Test
    fun `recalculates scores when settings change`() =
        runTest(testDispatcher) {
            // Given
            val venue = Venue(1, "Test", 0.0, 0.0)
            venueRepository.venues = listOf(venue)
            // 70 degrees is in default range (60-80) -> score 10 for temp
            val forecast =
                listOf(
                    WeatherPeriod(
                        number = 1,
                        name = "Today",
                        temperature = 70,
                        temperatureUnit = "F",
                        windSpeed = "5 mph",
                        shortForecast = "Clear",
                    ),
                )
            forecastService.forecast = forecast

            presenter.loadVenuesWithForecasts()
            advanceUntilIdle()

            val initialScore = presenter.state.value.venues[0].forecast[0]?.getEffectiveScore() ?: 0

            // When
            // Change min temp to 75 -> 70 is now outside range -> lower score
            settingsRepository.setSettings(PlayabilitySettings(minTemp = 75))
            advanceUntilIdle()

            // Then
            val newScore = presenter.state.value.venues[0].forecast[0]?.getEffectiveScore() ?: 0
            assertTrue(
                newScore < initialScore,
                "Score should decrease when temp is out of range. Initial: $initialScore, New: $newScore",
            )
        }

    @Test
    fun `current location updates state`() =
        runTest(testDispatcher) {
            // When
            val coordinate = Coordinate(45.0, -90.0)
            locationService.setLocation(coordinate)
            advanceUntilIdle()

            // Then
            assertEquals(coordinate, presenter.state.value.currentLocation)
        }
}

class FakeLogger : Logger {
    override fun d(
        tag: String,
        message: String,
    ) {
        println("D/$tag: $message")
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        println("E/$tag: $message")
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        println("I/$tag: $message")
    }
}

class FakeVenueRepository : VenueRepository {
    private val _venues = MutableStateFlow<List<Venue>>(emptyList())
    var venues: List<Venue>
        get() = _venues.value
        set(value) {
            _venues.value = value
        }
    var exception: Exception? = null

    override suspend fun getVenues(): List<Venue> {
        exception?.let { throw it }
        return venues
    }

    override suspend fun addVenue(venue: Venue) {
        venues = venues + venue
    }

    override suspend fun updateVenue(venue: Venue) {
        venues = venues.map { if (it.id == venue.id) venue else it }
    }

    override suspend fun removeVenue(venueId: Long) {
        venues = venues.filter { it.id != venueId }
    }

    override suspend fun syncVenues(venues: List<Venue>) {
        this.venues = venues
    }

    override fun getVenuesFlow(): Flow<List<Venue>> {
        return _venues
    }
}

class FakeForecastService : ForecastService {
    var forecast: List<WeatherPeriod> = emptyList()
    var exception: Exception? = null

    override suspend fun getForecastPoints(
        caw: String,
        gridX: String,
        gridY: String,
    ): JsonObject {
        return JsonObject(emptyMap())
    }

    override suspend fun getPoints(
        lat: Double,
        lon: Double,
    ): JsonObject {
        return JsonObject(emptyMap())
    }

    override suspend fun getForecastForLocation(
        lat: Double,
        lon: Double,
        venueId: Long?,
    ): List<WeatherPeriod> {
        exception?.let { throw it }
        return forecast
    }
}

class FakeSettingsRepository : SettingsRepository {
    private val settings =
        kotlinx.coroutines.flow.MutableStateFlow(
            PlayabilitySettings(),
        )

    override fun getSettings(): kotlinx.coroutines.flow.Flow<PlayabilitySettings> = settings

    override suspend fun updateSettings(settings: PlayabilitySettings) {
        this.settings.value = settings
    }

    fun setSettings(settings: PlayabilitySettings) {
        this.settings.value = settings
    }

    override fun getAllProfiles(): kotlinx.coroutines.flow.Flow<List<PlayabilityProfile>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override fun getProfileById(id: Long): kotlinx.coroutines.flow.Flow<PlayabilityProfile?> =
        kotlinx.coroutines.flow.flowOf(null)

    override suspend fun upsertProfile(profile: PlayabilityProfile) {
        // No-op for now
    }

    override suspend fun deleteProfile(id: Long) {
        // No-op for now
    }
}

class FakeLocationService : LocationService {
    private val _currentLocation = MutableStateFlow<Coordinate?>(null)
    override val currentLocation: StateFlow<Coordinate?> = _currentLocation

    override fun startLocationUpdates() {}
    override fun stopLocationUpdates() {}

    fun setLocation(coordinate: Coordinate?) {
        _currentLocation.value = coordinate
    }
}

class FakeReverseGeocodingService : ReverseGeocodingService {
    var address: String? = "Test Address"
    override suspend fun getAddress(
        lat: Double,
        lon: Double,
    ): String? = address
}

class FakeClock : Clock {
    var currentTime = kotlinx.datetime.Instant.fromEpochMilliseconds(0)
    override fun now(): kotlinx.datetime.Instant = currentTime
}
