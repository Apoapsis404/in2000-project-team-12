package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType

class ClimateDataRepository(
    private val climateDataSource: ClimateDataSource = ClimateDataSource(),
    private val stationRepository: ObsStationRepository = ObsStationRepository()
) {

    companion object {
        private const val DEFAULT_FIELDS = "sourceId,referenceTime,elementId,unit,value"
        private const val DEFAULT_MAX_DIST_KM = 5.0
        private const val MAX_STATIONS_LIMIT = 25
        private const val MAX_STATION_STEP = 5
        private const val MIN_MONTHS_REQUIRED = 36
    }

    /**
     * Fetch climate data for the given station.
     */
    suspend fun getClimateData(
        sourceId: String,
        elementId: String,
        referenceTime: String,
        fields: String = DEFAULT_FIELDS
    ): List<ClimateObs> {
        return climateDataSource.getClimateData(
            sourceId,
            elementId,
            referenceTime,
            fields
        )
    }

    /**
     * Fetch climate time series for the given station.
     */
    suspend fun getClimateTimeSeries(
        sourceId: String,
        elementId: String,
        fields: String = "validFrom,validTo"
    ): String {
        return climateDataSource.getClimateTimeSeries(sourceId, elementId, fields)
    }

    /**
     * Finds a station near the position that has a long enough time series for the given climate type.
     * Returns the validFrom date and the sourceId.
     */
    suspend fun getClimateTimeSeriesFromPos(
        position: LatLng,
        climateType: ClimateType,
        fields: String = "validFrom,validTo",
        maxCount: Int = 5
    ): Pair<String, String> {
        var currentMax = maxCount

        while (currentMax <= MAX_STATIONS_LIMIT) {
            val sources = stationRepository.getNearestStation(
                position.latitude,
                position.longitude,
                climateType,
                maxDist = DEFAULT_MAX_DIST_KM,
                maxCount = currentMax
            )

            for (source in sources) {
                try {
                    val result = climateDataSource.getClimateTimeSeries(source.id, climateType.elementId, fields)
                    if (result.isNotEmpty() && monthsSince(result) >= MIN_MONTHS_REQUIRED) {
                        stationRepository.addStationToCache(source, position, climateType)
                        return Pair(result, source.id)
                    }
                } catch (e: Exception) {
                    when (e) {
                        is NoAvailableTimeSeriesException, is NoDataException -> continue
                        else -> throw e
                    }
                }
            }
            currentMax += MAX_STATION_STEP
        }

        throw NoAvailableTimeSeriesException(
            "No available time series with at least $MIN_MONTHS_REQUIRED months of data found (up to $MAX_STATIONS_LIMIT stations tried)"
        )
    }

    /**
     * Try multiple nearby stations until data is found, optionally widening the search.
     */
    suspend fun getClimateDataWithFallback(
        position: LatLng,
        climateType: ClimateType,
        referenceTime: String,
        maxCount: Int = 5,
        fields: String = DEFAULT_FIELDS
    ): List<ClimateObs> {
        var currentMax = maxCount
        var lastTriedCount = 0

        while (currentMax <= MAX_STATIONS_LIMIT) {
            val sources = stationRepository.getNearestStation(
                position.latitude,
                position.longitude,
                climateType,
                maxDist = DEFAULT_MAX_DIST_KM,
                maxCount = currentMax
            )

            // Only try new stations found in this expansion
            for (source in sources.drop(lastTriedCount)) {
                try {
                    val result = climateDataSource.getClimateData(
                        source.id,
                        climateType.elementId,
                        referenceTime,
                        fields
                    )
                    if (result.isNotEmpty()) {
                        stationRepository.addStationToCache(source, position, climateType)
                        return result
                    }
                } catch (e: Exception) {
                    when (e) {
                        is NoAvailableTimeSeriesException, is NoDataException -> continue
                        else -> throw e
                    }
                }
            }

            lastTriedCount = currentMax
            currentMax += MAX_STATION_STEP
        }

        throw NoDataException(
            "No climate data found for ${climateType.elementId} (up to $MAX_STATIONS_LIMIT stations tried)"
        )
    }
}
