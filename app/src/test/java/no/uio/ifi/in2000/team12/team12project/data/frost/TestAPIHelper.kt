package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.Assert.*

class TestAPIHelper {

    @Test
    fun testConstructTimeSeries() {
        val timeSeries = constructTimeSeries("2016-01-01", "2017-01-01", "R2")
        val expected = "R2/2016-01-01/2017-01-01"
        assertEquals(expected, timeSeries)
    }

    @Test
    fun testIsFromMonth() {
        val referenceTime = "2016-01-01"
        assertEquals(true, isFromMonth(referenceTime, 1))
        assertEquals(false, isFromMonth(referenceTime, 2))
    }

    @Test
    fun testIsFromYear() {
        val referenceTime = "2016-01-01"
        assertEquals(true, isFromYear(referenceTime, 2016))
        assertEquals(false, isFromYear(referenceTime, 2017))
    }
}