package no.uio.ifi.in2000.team12.team12project.data.frost

import org.junit.Test
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team12.team12project.data.exceptions.BadRequestException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.UnauthorizedException
import kotlin.test.assertFailsWith

class TestAPIExceptionHandling {

    @Test
    fun testBadRequestException() = runTest {
        val dataSource = ClimateDataSource()
        assertFailsWith<BadRequestException> {
            dataSource.getClimateData("IN18701", "med(air_temperature P1M)", "1981-01-01")
        }
    }

    @Test
    fun testUnauthorizedException() = runTest {
        val dataSource = ObsStationDataSource()
        assertFailsWith<UnauthorizedException> {
            dataSource.getNearestStation(59.9423, 10.72, clientId = "invalid", clientSecret = "invalid")
        }
    }

    @Test
    fun testNoDataException() = runTest {
        val dataSource = ObsStationDataSource()
        assertFailsWith<NoDataException> {
            dataSource.getNearestStation(59.9423, 10.72, maxCount = 0)
        }
    }

    @Test
    fun testNoAvailableTimeSeriesException() = runTest {
        val dataSource = ClimateDataSource()
        assertFailsWith<NoAvailableTimeSeriesException> {
            dataSource.getClimateData("SN18701", "mean(air_temperature P1M)", "1982-01-01/1983-01-01")
        }
    }
}