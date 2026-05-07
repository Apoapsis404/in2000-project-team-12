package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTorrentialRain {
    private val torrentialRain = TorrentialRain()
    private val durationElement = "sum(duration_of_precipitation PT1H)"
    private val amountElement = "sum(precipitation_amount P1D)"


    @Test
    fun testCalculateTorrentialRain() = runTest {
        val monthsBack = 120u
        val duration = torrentialRain.getClimateData("SN18700", monthsBack, durationElement)
        val amount = torrentialRain.getClimateData("SN18700", monthsBack, amountElement)
        val map = torrentialRain.calculateTorrentialRainInstances(duration, amount)
        println(map)
    }

    @Test
    fun testNeedName() = runTest {
        val sourceId = "SN18700"
        val res = torrentialRain.isTorrentialRainThreat(sourceId)
        println(res)
    }

    @Test
    fun testAllStationsTorrentialRain() = runTest {
        val climateEvent = ClimateEvent()
        val stations = climateEvent.listOfTorrentialRainStations()
        println(stations)
    }

    @Test
    fun testMaxRainFallRate() = runTest {
        val climateEvent = ClimateEvent()
        val obsStationRepository = ObsStationRepository()
        val sources = obsStationRepository.getMetStations()
        println(sources)
//        val climateDataRepository = ClimateDataRepository()
//        val response = climateDataRepository.getClimateData(sources, "max(rainfall_rate PT1M", "2023-01-01/2023-01-02")
//        println(response)

    }
}