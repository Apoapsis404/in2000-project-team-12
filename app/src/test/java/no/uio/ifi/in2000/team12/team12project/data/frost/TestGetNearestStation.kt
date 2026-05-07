package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.launch
import org.junit.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

class GetNearestStationTest {

    @Test
    fun testGetNearestStation() = runTest {
        val dataSource = ObsStationsDataSource()

        launch {
            val station = dataSource.getNearestStation(59.9423, 10.72)
            assertEquals("SN18700", station[0].id)
        }
    }

    @Test
    fun testGetStationIdsFromList() = runTest {
        val dataSource = ObsStationsDataSource()

        launch {
            val station = dataSource.getNearestStation(59.9423, 10.72)
            val stationIds = getStationIdsFromList(station)
            val expected = "SN18700,SN18701,SN18703,SN499999010,SN18390"
            assertEquals(expected, stationIds)
        }
    }

    @Test
    fun testGetMetStation() = runTest {
        val dataSource = ObsStationsDataSource()

        val stations = dataSource.getMetStations()
        println(stations)
    }

}