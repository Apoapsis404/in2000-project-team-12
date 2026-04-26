package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.launch
import org.junit.Test
import kotlinx.coroutines.test.runTest


class GetClimateDataTest {

    @Test
    fun testGetClimateData() = runTest {
        val dataSource = ClimateDataSource()


        launch {
            val station = dataSource.getClimateData("SN18700", "mean(air_temperature P1M)", "R4/2016-01-01/2017-01-01")
            printClimateObsList(station)
        }
    }

    @Test
    fun testGetClimateDataFromNearest() = runTest {
        val climateDataSource = ClimateDataSource()
        val obsDataSource = ObsStationsDataSource()


        launch {
            val stations = obsDataSource.getNearestStation(59.9423, 10.72)
            val stationIds = getStationIdsFromList(stations)
            val station = climateDataSource.getClimateData(stationIds, "mean(min(air_temperature P1D) P1M)", "2016-01-01/2017-01-01")
            printClimateObsList(station)
        }
    }
}
