package no.uio.ifi.in2000.team12.team12project.data.frost

class ClimateEvent {
    val torrentialRain = TorrentialRain()
    val torrentialRainStations: List<String> = listOf("SN18700")

    // TODO wind speed!!!

    suspend fun listOfTorrentialRainStations(): List<String> {
        val stations = mutableListOf<String>()
        for (station in torrentialRainStations) {
            if (torrentialRain.isTorrentialRainThreat(station)) {
                stations.add(station)
            }
        }
        return stations
    }
}