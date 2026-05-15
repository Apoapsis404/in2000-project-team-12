package no.uio.ifi.in2000.team12.team12project.data.frost

import android.content.Context
import androidx.annotation.RawRes
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.HazardEvent
import kotlin.math.max

class ClimateHazard(
    private val climateDataSource: ClimateDataSource = ClimateDataSource(),
    private val obsStationDataSource: ObsStationDataSource = ObsStationDataSource()
) {

    private companion object {
        const val RAIN_ELEMENT = "sum(precipitation_amount PT1H)"
        const val WIND_ELEMENT = "max(wind_speed P1D)"
        const val MAX_NUM_MONTHS = 120u
        
        // 20mm in 1 hour is considered very heavy rain.
        const val TORRENTIAL_LIMIT_1H = 20.0
        
        // 17.2 m/s corresponds to "Strong Gale" on the Beaufort scale.
        const val STRONG_WIND_LIMIT = 17.2
    }

    /**
     * Finds MET stations with at least 3 torrential rain events in the last 10 years.
     * 
     * @return A list of [HazardEvent] containing coordinates and event count.
     */
    suspend fun getRainEventsFromStations(): List<HazardEvent> = coroutineScope {
        val sources = try {
            obsStationDataSource.getMetStations(RAIN_ELEMENT)
        } catch (_: Exception) {
            return@coroutineScope emptyList()
        }

        sources.map { source ->
            async {
                try {
                    val rawData = climateDataSource.getClimateData(
                        sources = source.id,
                        elementIds = RAIN_ELEMENT,
                        referenceTime = constructTimeSeries(MAX_NUM_MONTHS)
                    )

                    val events = analyzeRain(rawData)
                    if (events >= 3) {
                        HazardEvent(source.geometry.coordinates, events)
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    /**
     * Finds MET stations with an average of at least 5 strong wind events per year in the last 10 years.
     * 
     * @return A list of [HazardEvent] containing coordinates and average yearly event count.
     */
    suspend fun getWindEventsFromStations(): List<HazardEvent> = coroutineScope {
        val sources = try {
            obsStationDataSource.getMetStations(WIND_ELEMENT)
        } catch (_: Exception) {
            return@coroutineScope emptyList()
        }

        sources.map { source ->
            async {
                try {
                    val rawData = climateDataSource.getClimateData(
                        sources = source.id,
                        elementIds = WIND_ELEMENT,
                        referenceTime = constructTimeSeries(MAX_NUM_MONTHS)
                    )

                    if (rawData.isEmpty()) return@async null

                    val events = analyzeWind(rawData)
                    val years = max(yearsSince(rawData[0].referenceTime), 1)
                    val eventsPerYear = events / years

                    if (eventsPerYear >= 5) {
                        HazardEvent(source.geometry.coordinates, eventsPerYear)
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    private fun analyzeRain(data: List<ClimateObs>): Int {
        var foundEvents = 0
        for (observation in data) {
            val valObs = observation.observations.firstOrNull()?.value ?: 0.0
            if (valObs >= TORRENTIAL_LIMIT_1H) {
                foundEvents++
            }
        }
        return foundEvents
    }

    private fun analyzeWind(data: List<ClimateObs>): Int {
        var foundEvents = 0
        for (observation in data) {
            val valObs = observation.observations.firstOrNull()?.value ?: 0.0
            if (valObs >= STRONG_WIND_LIMIT) {
                foundEvents++
            }
        }
        return foundEvents
    }

    /**
     * Reads station coordinates from a JSON file in the raw resources.
     *
     * @param context The application context.
     * @param resourceId The resource ID of the JSON file in res/raw.
     * @return A list of LatLng coordinates.
     */
    fun readLatLngFromJson(context: Context, @RawRes resourceId: Int): List<LatLng> {
        val jsonString = try {
            context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            return emptyList()
        }
        return parseLatLngFromJson(jsonString)
    }

    /**
     * Parses a list of LatLng coordinates from a JSON string.
     *
     * @param jsonString The JSON string to parse.
     * @return A list of LatLng coordinates.
     */
    fun parseLatLngFromJson(jsonString: String): List<LatLng> {
        val stations = Json.decodeFromString<List<StationData>>(jsonString)
        // The coordinates are stored as [Longitude, Latitude]
        return stations.map { LatLng(it.coordinates[1], it.coordinates[0]) }
    }
}

@Serializable
private data class StationData(
    val coordinates: List<Double>,
    val event: Int
)