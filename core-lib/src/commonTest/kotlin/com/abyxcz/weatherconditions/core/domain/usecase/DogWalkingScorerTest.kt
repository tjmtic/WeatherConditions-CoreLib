package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.ScoringContext
import com.abyxcz.weatherconditions.core.domain.model.WeatherObservation
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import com.abyxcz.weatherconditions.core.domain.model.WeatherValue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

class DogWalkingScorerTest : FunSpec({

    val scorer = DogWalkingScorer()
    val base = Instant.parse("2026-06-10T08:00:00Z")
    fun at(hoursBefore: Int): String = (base - hoursBefore.hours).toString()

    fun period(
        temp: Int = 60,
        precipProb: Double = 0.0,
        precipAmount: Double = 0.0,
    ) = WeatherPeriod(
        name = "Walk window",
        startTime = base.toString(),
        temperature = temp,
        probabilityOfPrecipitation = WeatherValue(value = precipProb),
        precipitationAmount = WeatherValue(value = precipAmount),
        shortForecast = "Clear",
    )

    fun ctx(history: List<WeatherObservation>, p: WeatherPeriod = period()) =
        ScoringContext(periods = listOf(p), index = 0, history = history)

    val dryHistory = listOf(
        WeatherObservation(time = at(12), precipitationAmount = 0.0),
        WeatherObservation(time = at(24), precipitationAmount = 0.0),
        WeatherObservation(time = at(36), precipitationAmount = 0.0),
    )

    test("dry for 48h scores a perfect 40") {
        val result = scorer.score(ctx(dryHistory))
        result.total shouldBe 40
        result.factors.first { it.name == "recentRain" }.score shouldBe 25
    }

    test("rain 6h ago tanks the recentRain factor") {
        val history = dryHistory + WeatherObservation(time = at(6), precipitationAmount = 0.2)
        val result = scorer.score(ctx(history))
        result.factors.first { it.name == "recentRain" }.score shouldBe 2
        result.total shouldBeLessThan 20
    }

    test("rain ~30h ago is a partial penalty") {
        val history = listOf(WeatherObservation(time = at(30), precipitationAmount = 0.15)) + dryHistory
        val result = scorer.score(ctx(history))
        result.factors.first { it.name == "recentRain" }.score shouldBe 14
    }

    test("rain just outside the 48h window does not penalize") {
        val history = dryHistory + WeatherObservation(time = at(60), precipitationAmount = 0.3)
        val result = scorer.score(ctx(history))
        result.factors.first { it.name == "recentRain" }.score shouldBe 25
    }

    test("active rain during the walk lowers currentPrecip") {
        val result = scorer.score(ctx(dryHistory, period(precipProb = 70.0, precipAmount = 0.2)))
        result.factors.first { it.name == "currentPrecip" }.score shouldBe 2
    }

    test("no history yields an uncertain-but-usable score") {
        val result = scorer.score(ctx(emptyList()))
        result.factors.first { it.name == "recentRain" }.score shouldBe 18
        result.total shouldBeGreaterThan 20
    }

    test("extreme heat is uncomfortable for paws") {
        val result = scorer.score(ctx(dryHistory, period(temp = 98)))
        result.factors.first { it.name == "temperature" }.score shouldBe 1
    }
})
