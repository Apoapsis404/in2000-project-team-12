package no.uio.ifi.in2000.team12.team12project.data.frost

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation


/**
 * Repository for managing observation stations.
 * Provides caching of verified stations and nearest station lookup.
 */
class ObsStationRepository(
    private val obsStationDataSource: ObsStationDataSource = ObsStationDataSource(),
) {
    private companion object {
        const val TAG = "ObsStationRepository"
    }

    // Cache of stations verified to have data for a specific location and climate type.
    // Key is a combination of the requested location and the climate type.
    private val verifiedStationsCache = mutableMapOf<Pair<LatLng, ClimateType>, ObsStation>()

    /**
     * Finds the nearest observation stations to a given coordinate.
     * Uses a cache for stations previously verified to have valid data.
     *
     * @param latitude The latitude of the point.
     * @param longitude The longitude of the point.
     * @param climateType The climate type for which we need a station (used for cache lookup).
     * @param maxDist Maximum distance in kilometers for filtering results locally.
     * @param maxCount Maximum number of stations to fetch from the data source.
     * @return A list of [ObsStation]s within the specified distance.
     */
    suspend fun getNearestStation(
        latitude: Double,
        longitude: Double,
        climateType: ClimateType,
        maxDist: Double = 5.0,
        maxCount: Int = 5
    ): List<ObsStation> {
        val position = LatLng(latitude, longitude)
        val cacheKey = position to climateType

        // 1. Check if we have a verified station in cache for this exact position and type
        verifiedStationsCache[cacheKey]?.let {
            Log.d(TAG, "Cache hit for verified station at $position for $climateType: ${it.id}")
            return listOf(it)
        }

        return try {
            // 2. Fetch nearest stations from data source
            // Note: DataSource expects maxCount as an Int.
            val stations = obsStationDataSource.getNearestStation(
                latitude = latitude,
                longitude = longitude,
                maxCount = maxCount,
                element = climateType.elementId,
            )

            // 3. Filter by distance locally and return
            val filteredStations = stations.filter { it.distance <= maxDist }
            Log.d(TAG, "Fetched ${stations.size} stations, ${filteredStations.size} within ${maxDist}km")
            filteredStations
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching nearest stations for ($latitude, $longitude): ${e.message}", e)
            // Re-throw to allow higher-level repositories (like ClimateDataRepository) to handle 
            // specific API exceptions (e.g., NoDataException, UnauthorizedException)
            throw e
        }
    }

    /**
     * Caches a station that has been verified to have valid data for a specific location and climate type.
     * This avoids repeated API calls when multiple requests are made for the same location/type.
     *
     * @param station The [ObsStation] to cache.
     * @param position The [LatLng] position used for the request.
     * @param climateType The [ClimateType] associated with the station.
     */
    fun addStationToCache(station: ObsStation, position: LatLng, climateType: ClimateType) {
        verifiedStationsCache[position to climateType] = station
    }
}
