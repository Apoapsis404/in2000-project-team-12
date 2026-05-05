package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTorrentialRain {
    private val torrentialRain = TorrentialRain()

    @Test
    fun testGetTorrentialRain() = runTest {
        val obs = torrentialRain.getClimateData("SN18700", 1u, durationElement)
        printClimateObsList(obs)
    }

    @Test
    fun testGetPrecipitationAmount() = runTest {
        val obs = torrentialRain.getClimateData("SN18700", 1u, amountElement)
        printClimateObsList(obs)
    }

    @Test
    fun testCalculateTorrentialRain() = runTest {
        val monthsBack = 120u
        val duration = torrentialRain.getClimateData("SN18700", monthsBack, durationElement)
        val amount = torrentialRain.getClimateData("SN18700", monthsBack, amountElement)
        val map = torrentialRain.calculateTorrentialRain(duration, amount)
        println(map)
    }

    @Test
    fun testNeedName() = runTest {
        val sourceId = "SN18700"
        val res = torrentialRain.needsName(sourceId)
        println(res)
        assertTrue(res)
    }
}