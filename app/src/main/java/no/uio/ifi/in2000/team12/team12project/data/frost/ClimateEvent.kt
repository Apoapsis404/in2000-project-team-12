package no.uio.ifi.in2000.team12.team12project.data.frost

class ClimateEvent {
    val torrentialRain = TorrentialRain()
    val torrentialRainStations: List<String> = listOf("SN18700")
    val obsStationRepository: ObsStationRepository = ObsStationRepository()

    // TODO wind speed!!!

    suspend fun listOfTorrentialRainStations(): List<String> {
        val stations = mutableListOf<String>()
        for (station in obsStationRepository.getMetStations().split(",")) {
            if (torrentialRain.isTorrentialRainThreat(station)) {
                stations.add(station)
                println("Station $station is in area of torrential rain threat!")
            }
        }
        return stations
    }
}