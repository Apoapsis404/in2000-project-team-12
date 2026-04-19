package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation

class ObsStationRepository {

    private val obsStationDataSource = ObsStationsDataSource()
    private val cachedStations = mutableMapOf<Pair<LatLng, ClimateType>, ObsStation>()

    suspend fun getNearestStation(latitude: Double, longitude: Double, climateType: ClimateType, maxDist: Double = 5.0, maxCount: Int = 5): List<ObsStation> {
        val key = Pair(LatLng(latitude, longitude), climateType)
        if (cachedStations.containsKey(key)) {
            return listOf(cachedStations[key]!!)
        } else {
            val nearestStations =
                obsStationDataSource.getNearestStation(latitude, longitude, maxDist, maxCount)
            return nearestStations
        }
    }

    fun addStationToCache(station: ObsStation, position: LatLng, climateType: ClimateType) {
        cachedStations[Pair(position, climateType)] = station
    }
}