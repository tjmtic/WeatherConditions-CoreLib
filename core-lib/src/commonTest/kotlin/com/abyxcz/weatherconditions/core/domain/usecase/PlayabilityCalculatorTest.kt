package com.abyxcz.weatherconditions.core.domain.usecase

import com.abyxcz.weatherconditions.core.domain.model.PlayabilityProfile
import com.abyxcz.weatherconditions.core.domain.model.WeatherPeriod
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan

class PlayabilityCalculatorTest : FunSpec({

    val calculator = PlayabilityCalculator()
    val defaultProfile = PlayabilityProfile(
        name = "Default",
        minTemp = 60,
        maxTemp = 85,
        maxWindSpeed = 15,
        maxPrecipitation = 20
    )

    context("Temperature Scoring") {
        withData(
            nameFn = { "Temp ${it.first} should score ${it.second}" },
            Triple(70, 10, "Perfect"),
            Triple(58, 7, "Slightly Cold"),
            Triple(87, 7, "Slightly Hot"),
            Triple(52, 5, "Cold"),
            Triple(92, 5, "Hot"),
            Triple(40, 2, "Extreme")
        ) { (temp, expectedScore, _) ->
            val period = createWeatherPeriod(temperature = temp)
            val result = calculator.calculateScore(defaultProfile, listOf(period), 0)
            result.temperatureScore shouldBe expectedScore
        }
    }

    context("Precipitation Scoring") {
        withData(
            nameFn = { "Precip ${it.first}% should score ${it.second}" },
            Triple(0.0, 10, "None"),
            Triple(15.0, 8, "Light"),
            Triple(35.0, 5, "Moderate"),
            Triple(60.0, 2, "Heavy")
        ) { (precip, expectedScore, _) ->
            val period = createWeatherPeriod(precipProb = precip)
            val result = calculator.calculateScore(defaultProfile, listOf(period), 0)
            result.precipitationScore shouldBe expectedScore
        }
    }

    context("Overnight Impact (Lookback Penalty)") {
        test("Heavy rain yesterday penalizes today") {
            val yesterday = createWeatherPeriod(isDaytime = false, precipAmount = 1.0)
            val today = createWeatherPeriod(isDaytime = true)
            
            val result = calculator.calculateScore(defaultProfile, listOf(yesterday, today), 1)
            
            result.lookbackPenalty shouldBe -15
            result.totalScore shouldBeLessThan 30 // Should be significantly reduced
        }

        test("No rain yesterday has no penalty") {
            val yesterday = createWeatherPeriod(isDaytime = false, precipAmount = 0.0, precipProb = 0.0)
            val today = createWeatherPeriod(isDaytime = true)
            
            val result = calculator.calculateScore(defaultProfile, listOf(yesterday, today), 1)
            
            result.lookbackPenalty shouldBe 0
        }
    }
})

private fun createWeatherPeriod(
    temperature: Int = 72,
    precipProb: Double = 0.0,
    precipAmount: Double = 0.0,
    isDaytime: Boolean = true,
    windSpeed: String = "5 mph",
    shortForecast: String = "Sunny"
) = WeatherPeriod(
    number = 1,
    name = "Test",
    isDaytime = isDaytime,
    temperature = temperature,
    probabilityOfPrecipitation = com.abyxcz.weatherconditions.core.domain.model.WeatherValue("percent", precipProb),
    precipitationAmount = com.abyxcz.weatherconditions.core.domain.model.WeatherValue("in", precipAmount),
    windSpeed = windSpeed,
    shortForecast = shortForecast
)
