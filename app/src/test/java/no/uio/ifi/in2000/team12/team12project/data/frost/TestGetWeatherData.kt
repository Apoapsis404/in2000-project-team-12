package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.launch
import org.junit.Test
import kotlinx.coroutines.test.runTest


class GetWeatherDataTest {

    @Test
    fun testGetWeatherData() = runTest {
        val dataSource = WeatherDataSource()


        launch {
            val station = dataSource.getWeatherData("SN18700", "mean(min(air_temperature P1D) P1M)", "R4/2016-01-01/2017-01-01")
        }
    }

    @Test
    fun testGetWeatherDataFromNearest() = runTest {
        val weatherDataSource = WeatherDataSource()
        val obsDataSource = ObsStationsDataSource()


        launch {
            val stations = obsDataSource.getNearestStation(59.9423, 10.72)
            val stationIds = obsDataSource.getStationIdsFromList(stations)
            val station = weatherDataSource.getWeatherData(stationIds, "mean(min(air_temperature P1D) P1M)", "R4/2016-01-01/2017-01-01")
        }
    }
}
