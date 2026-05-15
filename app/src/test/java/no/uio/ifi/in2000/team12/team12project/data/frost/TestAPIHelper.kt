package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.Observation
import org.junit.Test
import org.junit.Assert.*

class TestAPIHelper {

    @Test
    fun testConstructTimeSeries() {
        val currentDate = "2023-03-01"
        val timeSeries = constructTimeSeries(5u, currentDate)
        val expected = "2022-10-01/${getStartOfDate(currentDate)}"
        assertEquals(expected, timeSeries)

        val currentDate2 = "2022-03-28"
        val timeSeries2 = constructTimeSeries(21u, currentDate2)
        val expected2 = "2020-06-01/${getStartOfDate(currentDate2)}"
        assertEquals(expected2, timeSeries2)

        println("Time series from current date: ${constructTimeSeries(17u)}")
    }

    @Test
    fun testGetCurrentDate() {
        val currentDate = getCurrentDate()
        println(currentDate)
    }

    @Test
    fun testGetStartOfDate() {
        val currentDate = "2023-03-02"
        val startOfDate = getStartOfDate(currentDate)
        val expected = "2023-03-01"
        assertEquals(expected, startOfDate)

        val currentDate2 = "2022-07-28"
        val startOfDate2 = getStartOfDate(currentDate2)
        val expected2 = "2022-07-01"
        assertEquals(expected2, startOfDate2)
    }

    @Test
    fun testLastNMonths() {
        val currentDate = "2023-03-01"
        val last5Months = lastNMonths(5u, currentDate)
        val expected = "2022-10-01"
        assertEquals(expected, last5Months)

        val last2Months = lastNMonths(2u, currentDate)
        val expected2 = "2023-01-01"
        assertEquals(expected2, last2Months)

        val last12Months = lastNMonths(12u, currentDate)
        val expected3 = "2022-03-01"
        assertEquals(expected3, last12Months)

        val last15Months = lastNMonths(15u, currentDate)
        val expected4 = "2021-12-01"
        assertEquals(expected4, last15Months)

        println(lastNMonths(5u, getCurrentDate()))
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

    @Test
    fun testFindMaxObs() {
        val climateObs = ClimateObs(
            sourceId = "SN18700",
            referenceTime = "2023-01-01",
            observations = listOf(
                Observation(elementId = "mean_wind_speed", value = 5.0, unit = "m/s"),
                Observation(elementId = "mean_wind_speed", value = 10.0, unit = "m/s"),
                Observation(elementId = "mean_wind_speed", value = 15.0, unit = "m/s")
            )
        )
        val max = findMaxObs(climateObs)
        assertEquals(15.0, max, 0.001)
    }
}