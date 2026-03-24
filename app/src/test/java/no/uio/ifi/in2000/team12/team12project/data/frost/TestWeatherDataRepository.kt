package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import org.junit.Test

class TestWeatherDataRepository {

    @Test
    fun testGetWeatherDataFromYear() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700,SN18701,SN18703,SN499999010,SN18390",
            "mean(air_temperature P1M)",
            "R4/2016-01-01/2017-01-01"
        )
        val obsFromYear = weatherDataRepository.getObsFromYear(2016)
        printWeatherObservations(obsFromYear)
    }

    @Test
    fun testGetWeatherDataFromMonth() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700,SN18701,SN18703,SN499999010,SN18390",
            "mean(air_temperature P1M)",
            "R4/2016-01-01/2017-01-01"
        )
        val obsFromMonth = weatherDataRepository.getObsFromMonth(Months.FEBRUARY)
        printWeatherObservations(obsFromMonth)
    }

    @Test
    fun testFindMeanValue_FromMonth() = runTest {
        val weatherDataRepository = WeatherDataRepository()
        weatherDataRepository.getWeatherData(
            "SN18700,SN18701,SN18703,SN499999010,SN18390",
            "mean(air_temperature P1M)",
            "R4/2016-01-01/2017-01-01"
        )
        val obsFromMonth = weatherDataRepository.getObsFromMonth(Months.DECEMBER)
        val meanValue = weatherDataRepository.findMeanValue(obsFromMonth)
        println("Mean value: $meanValue")
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
        println("${weatherChartData.years.size}, ${weatherChartData.years}")
        println("${weatherChartData.values.size}, ${weatherChartData.values}")
    }
}