package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs


/**
 * Repository used by TestWeatherDataRepository.
 * Keeps an internal cache of the last fetched observations (weatherObsList)
 * so that getObsFromYear/getObsFromMonth work as expected in tests.
 */
class UIO_GPT_WeatherDataRepository(
    private val weatherDataSource: WeatherDataSource = WeatherDataSource(),
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
     * Internal state used by getObsFromYear/getObsFromMonth tests.
     * It is always overwritten on each successful data fetch.
     */
    private val weatherObsList = mutableListOf<WeatherObs>()

    /**
     * Fetch weather data for the nearest station to the given position.
     * Also updates internal weatherObsList cache.
     */
    suspend fun getWeatherData(
        position: LatLng,
        climateType: ClimateType,
        referenceTime: String,
        fields: String = DEFAULT_FIELDS
    ): List<WeatherObs> {
        weatherObsList.clear()

        val stations = stationRepository.getNearestStation(
            position.latitude,
            position.longitude,
            climateType
        )

        val sourceId = stations.firstOrNull()?.id
            ?: throw NoAvailableTimeSeriesException("No nearby stations found")

        val result = weatherDataSource.getWeatherData(
            sourceId,
            climateType.elementId,
            referenceTime,
            fields
        )

        weatherObsList.addAll(result)
        return result
    }

    /**
     * Try multiple nearby stations until data is found, optionally widening the search.
     * Mirrors the behavior of the original getWeatherDataFailing but avoids recursion.
     * Also updates internal weatherObsList cache on success.
     */
    suspend fun getWeatherDataFailing(
        position: LatLng,
        climateType: ClimateType,
        referenceTime: String,
        minCount: Int = 0,
        maxCount: Int = 5,
        fields: String = DEFAULT_FIELDS
    ): List<WeatherObs> {
        weatherObsList.clear()

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
                    weatherDataSource.getWeatherData(
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
                    weatherObsList.clear()
                    weatherObsList.addAll(result)
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

    /**
     * Returns observations from the internally cached list whose referenceTime is in the given year.
     * Used by the integration tests after getWeatherDataFailing has populated weatherObsList.
     */
    fun getObsFromYear(year: Int): List<WeatherObs> {
        if (weatherObsList.isEmpty()) return emptyList()

        return weatherObsList.filter { obs ->
            extractYear(obs.referenceTime) == year
        }
    }

    /**
     * Returns observations from the internally cached list whose referenceTime is in the given month.
     * Used by the integration tests after getWeatherDataFailing has populated weatherObsList.
     */
    fun getObsFromMonth(month: Months): List<WeatherObs> {
        if (weatherObsList.isEmpty()) return emptyList()

        return weatherObsList.filter { obs ->
            extractMonth(obs.referenceTime) == month.value
        }
    }

    /**
     * Compute the mean of the per-observation means.
     * Tests assume the list is non-empty, so we keep the non-null Double return type.
     */
    fun findMeanValue(weatherObsList: List<WeatherObs>): Double {
        require(weatherObsList.isNotEmpty()) {
            "Cannot compute mean value of an empty list"
        }

        // Mean of each WeatherObs
        val obsMeans = weatherObsList.map { obs ->
            findMeanObs(obs)
        }

        // Mean of those means
        return obsMeans.average()
    }

    /**
     * Compute the mean value of a single WeatherObs.
     * Tests assume observations is non-empty.
     */
    fun findMeanObs(weatherObs: WeatherObs): Double {
        val observations = weatherObs.observations
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
    fun getYearAndData(weatherObsList: List<WeatherObs>): WeatherChartData {
        val years = mutableListOf<Double>()
        val values = mutableListOf<Double>()

        weatherObsList.forEach { obs ->
            val year = extractYear(obs.referenceTime)
            if (year != null) {
                years += year.toDouble()
                values += findMeanObs(obs)
            }
        }

        return WeatherChartData(
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
