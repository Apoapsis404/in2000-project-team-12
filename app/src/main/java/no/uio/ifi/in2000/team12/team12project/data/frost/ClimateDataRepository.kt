package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType


class ClimateDataRepository(
    private val climateDataSource: ClimateDataSource = ClimateDataSource(),
    private val stationRepository: ObsStationRepository = ObsStationRepository()
) {

    companion object {
        private const val DEFAULT_FIELDS =
            "sourceId,referenceTime,elementId,unit,value"

        private const val DEFAULT_MAX_DIST_KM = 5.0
        private const val MAX_STATIONS_LIMIT = 25
        private const val MAX_STATION_STEP = 5
    }

    /**
     * Fetch climate data for the given station
     */
    suspend fun getClimateData(
        sourceId: String,
        elementId: String,
        referenceTime: String,
        fields: String = DEFAULT_FIELDS
    ): List<ClimateObs> {
        val result = climateDataSource.getClimateData(
            sourceId,
            elementId,
            referenceTime,
            fields
        )
        return result
    }

    suspend fun getClimateTimeSeries(sourceId: String, elementId: String, fields: String = "validFrom"): String {
        try {
            val result = climateDataSource.getClimateTimeSeries(sourceId, elementId, fields)
            return result
        } catch (e: Exception) { //TODO handle specific exceptions
            throw e
        }
    }

    /**
     * Try multiple nearby stations until data is found, optionally widening the search.
     */
    suspend fun getClimateDataFailing(
        position: LatLng,
        climateType: ClimateType,
        referenceTime: String,
        minCount: Int = 0,
        maxCount: Int = 5,
        fields: String = DEFAULT_FIELDS
    ): List<ClimateObs> {
        var currentMin = minCount
        var currentMax = maxCount

        while (currentMax <= MAX_STATIONS_LIMIT) {
            val sources = stationRepository.getNearestStation(
                position.latitude,
                position.longitude,
                climateType,
                maxDist = DEFAULT_MAX_DIST_KM,
                maxCount = currentMax
            )

            for (source in sources.drop(currentMin)) {
                val result = try {
                    climateDataSource.getClimateData(
                        source.id,
                        climateType.elementId,
                        referenceTime,
                        fields
                    )
                } catch (e: NoAvailableTimeSeriesException) {
                    // No time series for this station; skip to next
                    println("No time series for station ${e.message}")
                    continue
                }

                if (result.isNotEmpty()) {
                    stationRepository.addStationToCache(source, position, climateType)
                    return result
                }
            }

            currentMin = currentMax
            currentMax += MAX_STATION_STEP
        }

        throw NoAvailableTimeSeriesException(
            "No available time series (up to $MAX_STATIONS_LIMIT stations tried)"
        )
    }
}
