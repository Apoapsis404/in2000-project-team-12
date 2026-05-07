package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.gson.GsonBuilder
import no.uio.ifi.in2000.team12.team12project.model.frost.ThreatLevel
import java.io.File

class ClimateEvent {
    val torrentialRain = TorrentialRain()
    val obsStationRepository: ObsStationRepository = ObsStationRepository()

    // TODO wind speed!!!

    suspend fun listOfTorrentialRainStations(): Map<String, ThreatLevel> {
        val stations = mutableMapOf<String, ThreatLevel>()
        for (station in obsStationRepository.getMetStations().split(",")) {
            val stationThreatLevel = torrentialRain.isTorrentialRainThreat(station)
            if (stationThreatLevel != ThreatLevel.NONE) {
                stations[station] = stationThreatLevel
                println("Station $station is in area of torrential rain threat level $stationThreatLevel!")
            }
        }
        return stations
    }

}
