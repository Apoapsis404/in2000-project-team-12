package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs

class TorrentialRain(
    private val climateDataRepository: ClimateDataRepository = ClimateDataRepository()
) {

    private val torrentialRainRate = 0.5

    suspend fun getPrecipitationDuration(sourceId: String): List<ClimateObs> {
        val elementId = "sum(duration_of_precipitation PT1H)"
        val obs = climateDataRepository.getClimateData(sourceId, elementId,constructTimeSeries(1u))
        return obs
    }

    /*
     * Extract year and month and sum up all and
     */
    suspend fun
}