package no.uio.ifi.in2000.team12.team12project.data.frost

import org.junit.Test
import kotlin.test.assertEquals

class TestClimateHazard {

    val climateHazard = ClimateHazard()

    @Test
    fun testParseLatLngFromJson() {
        val jsonString = """
            [
              {"coordinates": [10.72, 59.9423], "event": 9},
              {"coordinates": [20.0930685, 69.5887], "event": 7}
            ]
        """.trimIndent()
        
        val result = climateHazard.parseLatLngFromJson(jsonString)
        
        assertEquals(2, result.size)
        // Check first station (Longitude 10.72, Latitude 59.9423)
        assertEquals(59.9423, result[0].latitude, 0.0001)
        assertEquals(10.72, result[0].longitude, 0.0001)
        
        // Check second station
        assertEquals(69.5887, result[1].latitude, 0.0001)
        assertEquals(20.0930685, result[1].longitude, 0.0001)
    }

}