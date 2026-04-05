package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import org.junit.Test
import kotlin.test.assertEquals

class TestWeatherDataRepository {

    @Test
    fun testGetWeatherDataFromYear() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700,SN18701,SN18703,SN499999010,SN18390",
            "mean(air_temperature P1M)",
            "R4/2016-01-01/2017-01-01"
        )
        var testYear = 2016
        val obsFromYear = weatherDataRepository.getObsFromYear(testYear)
        var getYear = 0
        obsFromYear.forEach { obs ->
            getYear = obs.referenceTime.substring(0, 4).toInt()
            assertEquals(testYear, getYear)
        }
        testYear = 2018
        val obsFromYear2 = weatherDataRepository.getObsFromYear(testYear)
        obsFromYear2.forEach { obs ->
            getYear = obs.referenceTime.substring(0, 4).toInt()
            assertEquals(testYear, getYear)
        }
    }

    @Test
    fun testGetWeatherDataFromMonth() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700,SN18701,SN18703,SN499999010,SN18390",
            "mean(air_temperature P1M)",
            "R4/2016-01-01/2017-01-01"
        )

        var testMonth = Months.FEBRUARY

        val obsFromMonth = weatherDataRepository.getObsFromMonth(testMonth)
        var getMonth = 0
        obsFromMonth.forEach { obs ->
            getMonth = obs.referenceTime.substring(5, 7).toInt()
            assertEquals(testMonth.value, getMonth)
        }

        testMonth = Months.OCTOBER

        val obsFromMonth2 = weatherDataRepository.getObsFromMonth(testMonth)
        obsFromMonth2.forEach { obs ->
            getMonth = obs.referenceTime.substring(5, 7).toInt()
            assertEquals(testMonth.value, getMonth)
        }
    }

    @Test
    fun testFindMeanValue_FromMonth() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700,SN18701,SN18703,SN499999010,SN18390",
            "mean(air_temperature P1M)",
            "R4/2016-01-01/2017-01-01"
        )
        var testMonth = Months.DECEMBER
        val obsFromMonth = weatherDataRepository.getObsFromMonth(testMonth)
        var meanValue = weatherDataRepository.findMeanValue(obsFromMonth)

        val expected = -0.23749999999999996
        assertEquals(expected, meanValue)

        testMonth = Months.FEBRUARY
        val obsFromMonth2 = weatherDataRepository.getObsFromMonth(testMonth)
        meanValue = weatherDataRepository.findMeanValue(obsFromMonth2)
        val expected2 = -1.40625
        assertEquals(expected2, meanValue)
    }

    @Test
    fun testGetYearAndDate() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700",
            "mean(air_temperature P1M)",
            "R10/2010-01-01/2011-01-01"
        )
        val obsFromMonth = weatherDataRepository.getObsFromMonth(Months.DECEMBER)
        val weatherChartData = weatherDataRepository.getYearAndData(obsFromMonth)

        // Testing that both years and values are the same size which is required for by the graph
        assertEquals(weatherChartData.years.size, weatherChartData.values.size)

        val expectedYears: List<Double> = (2010..2019).map { it.toDouble() }
        assertEquals(expectedYears, weatherChartData.years)
    }
}