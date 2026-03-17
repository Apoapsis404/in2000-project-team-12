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
        }
    }

    @Test
    fun testGetStationIdsFromList() = runTest {
        val dataSource = ObsStationsDataSource()

        launch {
            val station = dataSource.getNearestStation(59.9423, 10.72)
            val stationIds = dataSource.getStationIdsFromList(station)
            val expected = "SN18317,SN18310,SN18645,SN18162,SN18690"
            assertEquals(expected, stationIds)
        }
    }

}