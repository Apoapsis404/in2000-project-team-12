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
    fun testParseLatLngFromJson() {
        val jsonString = """
            [
              {"coordinates": [10.72, 59.9423], "event": 9},
              {"coordinates": [20.0930685, 69.5887], "event": 7}
            ]
        """.trimIndent()
        
        val result = torrentialRain.parseLatLngFromJson(jsonString)
        
        assertEquals(2, result.size)
        // Check first station (Longitude 10.72, Latitude 59.9423)
        assertEquals(59.9423, result[0].latitude, 0.0001)
        assertEquals(10.72, result[0].longitude, 0.0001)
        
        // Check second station
        assertEquals(69.5887, result[1].latitude, 0.0001)
        assertEquals(20.0930685, result[1].longitude, 0.0001)
    }

}