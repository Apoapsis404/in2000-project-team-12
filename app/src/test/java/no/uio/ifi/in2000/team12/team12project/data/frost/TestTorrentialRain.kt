package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTorrentialRain {
    private val torrentialRain = TorrentialRain()

    @Test
    fun testGetTorrentialRain() = runTest {
        val obs = torrentialRain.getPrecipitationDuration("SN18700", 1u)
        printClimateObsList(obs)
    }

    @Test
    fun testGetPrecipitationAmount() = runTest {
        val obs = torrentialRain.getPrecipitationAmount("SN18700", 1u)
        printClimateObsList(obs)
    }

    @Test
    fun testCalculateTorrentialRain() = runTest {
        val monthsBack = 120u
        val duration = torrentialRain.getPrecipitationDuration("SN18700", monthsBack)
        val amount = torrentialRain.getPrecipitationAmount("SN18700", monthsBack)
        val map = torrentialRain.calculateTorrentialRain(duration, amount)
        println(map)
    }
}