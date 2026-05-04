package no.uio.ifi.in2000.team12.team12project.data.frost

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestTorrentialRain {
    private val torrentialRain = TorrentialRain()

    @Test
    fun testGetTorrentialRain() = runTest {
        val obs = torrentialRain.getPrecipitationDuration("SN18700")
        printClimateObsList(obs)
    }
}