package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TestClimateDataSource {

    private lateinit var climateDataSource: ClimateDataSource
    private lateinit var obsDataSource: ObsStationDataSource

    @Before
    fun setUp() {
        climateDataSource = ClimateDataSource()
        obsDataSource = ObsStationDataSource()
    }

    @Test
    fun testGetClimateData() = runTest {
        val observations = climateDataSource.getClimateData(
            sources = "SN18700",
            elementIds = "mean(air_temperature P1M)",
            referenceTime = "R4/2016-01-01/2017-01-01"
        )
        
        assertNotNull("Observations should not be null", observations)
        assertTrue("Climate data observations list should not be empty", observations.isNotEmpty())
    }

    @Test
    fun testGetClimateDataFromNearest() = runTest {
        val stations = obsDataSource.getNearestStation(59.9423, 10.72)
        assertTrue("Should find at least one nearest station", stations.isNotEmpty())
        
        val stationIds = getStationIdsFromList(stations)
        val observations = climateDataSource.getClimateData(
            sources = stationIds,
            elementIds = "mean(min(air_temperature P1D) P1M)",
            referenceTime = "2016-01-01/2017-01-01"
        )
        
        assertNotNull("Observations should not be null", observations)
        assertTrue("Climate data from nearest station should not be empty", observations.isNotEmpty())
    }

    @Test
    fun testGetClimateTimeSeries() = runTest {
        val expected = "1993-11-07T00:00:00.000Z"
        val actual = climateDataSource.getClimateTimeSeries("SN18700", "sum(duration_of_precipitation PT1H)")
        
        assertNotNull("Time series result should not be null", actual)
        assertTrue("Time series should contain expected date: $expected", actual.contains(expected))
    }
}
