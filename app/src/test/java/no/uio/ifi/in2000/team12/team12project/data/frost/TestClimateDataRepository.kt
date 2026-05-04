package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.Observation
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestClimateDataRepository {

    private val repository = ClimateDataRepository()
    private val oslo = LatLng(59.91, 10.75)

    @Test
    fun testFindMeanValue() {
        val observations = listOf(
            ClimateObs(
                sourceId = "SN18700",
                referenceTime = "2023-01-01",
                observations = listOf(
                    Observation("temp", 10.0, "C"),
                    Observation("temp", 20.0, "C")
                )
            ),
            ClimateObs(
                sourceId = "SN18701",
                referenceTime = "2023-02-01",
                observations = listOf(
                    Observation("temp", 30.0, "C")
                )
            )
        )
        // Mean of obs1: (10+20)/2 = 15.0
        // Mean of obs2: 30.0
        // Total mean: (15+30)/2 = 22.5
        val result = findMeanValue(observations)
        assertEquals(22.5, result, 0.001)
    }

    @Test
    fun testFindMeanObs() {
        val obs = ClimateObs(
            sourceId = "SN18700",
            referenceTime = "2023-01-01",
            observations = listOf(
                Observation("temp", 5.0, "C"),
                Observation("temp", 15.0, "C")
            )
        )
        val result = findMeanObs(obs)
        assertEquals(10.0, result, 0.001)
    }

    @Test
    fun testGetYearAndData() {
        val observations = listOf(
            ClimateObs(
                sourceId = "SN18700",
                referenceTime = "2023-01-01",
                observations = listOf(Observation("temp", 10.0, "C"))
            ),
            ClimateObs(
                sourceId = "SN18701",
                referenceTime = "2024-01-01",
                observations = listOf(Observation("temp", 20.0, "C"))
            )
        )
        val result = getYearAndData(observations)
        assertEquals(listOf(2023.0, 2024.0), result.years)
        assertEquals(listOf(10.0, 20.0), result.values)
    }

    @Test
    fun testGetWeatherDataIntegration() = runTest {
        // Integration test using real Frost API
        val climateType = ClimateType.MEAN_AIR_TEMP
        val referenceTime = "2023-01-01/2023-12-31"

        val result = repository.getClimateDataFailing(oslo, climateType, referenceTime)

        assertTrue(result.isNotEmpty(), "Should return weather observations")

        // Test getObsFromYear and getObsFromMonth using the populated internal state
        val obs2023 = getObsFromYear(2023, result)
        assertTrue(obs2023.isNotEmpty(), "Should find observations for 2023")

        val janObs = getObsFromMonth(Months.JANUARY, result)
        janObs.forEach { obs ->
            assertTrue(obs.referenceTime.contains("-01-"), "Observation should be from January")
        }
    }

    @Test
    fun testGetWeatherDataFailingIntegration() = runTest {
        // Integration test for the fallback logic
        val climateType = ClimateType.MEAN_AIR_TEMP
        val referenceTime = "2022-01-01/2022-12-31"

        val result = repository.getClimateDataFailing(oslo, climateType, referenceTime)
        assertTrue(result.isNotEmpty(), "Should return observations using fallback logic")
    }
}
