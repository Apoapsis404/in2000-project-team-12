package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs


data class ClimateChartData(
    val years: List<Double>,
    val values: List<Double>
)


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
     * Fetch climate data for the nearest station to the given position.
     * Also updates internal weatherObsList cache.
     */
    suspend fun getClimateData(
        position: LatLng,
        climateType: ClimateType,
        referenceTime: String,
        fields: String = DEFAULT_FIELDS
    ): List<ClimateObs> {
        val stations = stationRepository.getNearestStation(
            position.latitude,
            position.longitude,
            climateType
        )

        val sourceId = stations.firstOrNull()?.id
            ?: throw NoAvailableTimeSeriesException("No nearby stations found")

        val result = climateDataSource.getClimateData(
            sourceId,
            climateType.elementId,
            referenceTime,
            fields
        )
        return result
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

    fun getObsFromYear(year: Int, climateObsList: List<ClimateObs>): List<ClimateObs> {
        if (climateObsList.isEmpty()) return emptyList()

        return climateObsList.filter { obs ->
            extractYear(obs.referenceTime) == year
        }
    }

    /**
     * Returns observations from the internally cached list whose referenceTime is in the given month.
     * Used by the integration tests after getWeatherDataFailing has populated weatherObsList.
     */
    fun getObsFromMonth(month: Months, climateObsList: List<ClimateObs>): List<ClimateObs> {
        if (climateObsList.isEmpty()) return emptyList()

        return climateObsList.filter { obs ->
            extractMonth(obs.referenceTime) == month.value
        }
    }

    /**
     * Compute the mean of the per-observation means.
     * Tests assume the list is non-empty, so we keep the non-null Double return type.
     */
    fun findMeanValue(climateObsList: List<ClimateObs>): Double {
        require(climateObsList.isNotEmpty()) {
            "Cannot compute mean value of an empty list"
        }

        // Mean of each WeatherObs
        val obsMeans = climateObsList.map { obs ->
            findMeanObs(obs)
        }

        // Mean of those means
        return obsMeans.average()
    }

    /**
     * Compute the mean value of a single WeatherObs.
     * Tests assume observations is non-empty.
     */
    fun findMeanObs(climateObs: ClimateObs): Double {
        val observations = climateObs.observations
        require(observations.isNotEmpty()) {
            "Cannot compute mean of WeatherObs with no observations"
        }

        val sum = observations.sumOf { it.value }
        return sum / observations.size
    }

    /**
     * Convert a list of WeatherObs into WeatherChartData.
     * Years are returned as Double to match existing tests (2023.0, 2024.0).
     */
    fun getYearAndData(climateObsList: List<ClimateObs>): ClimateChartData {
        val years = mutableListOf<Double>()
        val values = mutableListOf<Double>()

        climateObsList.forEach { obs ->
            val year = extractYear(obs.referenceTime)
            if (year != null) {
                years += year.toDouble()
                values += findMeanObs(obs)
            }
        }

        return ClimateChartData(
            years = years,
            values = values
        )
    }

    // --- Helper functions for parsing referenceTime ---

    /**
     * Extract year from referenceTime, assuming format like "YYYY-MM-DD..." (e.g. "2023-01-01").
     * Returns null if parsing fails.
     */
    private fun extractYear(referenceTime: String): Int? =
        referenceTime.takeIf { it.length >= 4 }
            ?.substring(0, 4)
            ?.toIntOrNull()

    /**
     * Extract month (1-12) from referenceTime, assuming format like "YYYY-MM-DD..." (e.g. "2023-01-01").
     * Returns null if parsing fails.
     */
    private fun extractMonth(referenceTime: String): Int? =
        referenceTime.takeIf { it.length >= 7 }
            ?.substring(5, 7)
            ?.toIntOrNull()
}
