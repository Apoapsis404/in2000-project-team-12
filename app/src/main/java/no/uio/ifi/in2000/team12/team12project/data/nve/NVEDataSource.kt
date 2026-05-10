package no.uio.ifi.in2000.team12.team12project.data.nve

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.nve.FloodWarning
import java.io.BufferedReader
import java.io.InputStreamReader
import android.content.Context

class NVEDataSource(val context: Context){
    private val url: String = "https://api01.nve.no/hydrology/forecast/flood/v1.0.10/api/Warning/1/2016-01-01/2026-01-01"
    private val json = Json { ignoreUnknownKeys = true }

    /*
    * Denne metoden henter varslinger fra NVE. Den flitrerer for kommuner som har hatt varselsnivå 4 mer enn 7 ganger.
    *Den returnerer em liste med FloodWarning objekter fra kommunene som oppfyller kravene
    * */
    suspend fun getFloodWarning(): List<FloodWarning> = withContext(Dispatchers.IO) {
        val response: HttpResponse = KtorClient.client.get(url)

        if (response.status.value !in 200..299) {
            throw Exception("Error getting flood warnings: ${response.status}")
        }
        val responseBodyString = response.bodyAsText()
        val allWarnings = json.decodeFromString<List<FloodWarning>>(responseBodyString)

        val coordinateMap = loadCoordinates()

        val relevantEventIds = allWarnings
            .filter { it.activityLevel == 4 }
            .groupBy { it.eventId }
            .filter { it.value.size >= 8 }
            .keys

        allWarnings
            .filter { it.eventId in relevantEventIds }
            .map { warning ->
                warning.copy(
                    municipalityList = warning.municipalityList.mapNotNull { municipality ->
                                coordinateMap[municipality.id]?.let{(lat, lon) ->
                                    municipality.copy(lat = lat, lon = lon)
                                }
                            }
                        )
            }
    }

    /*
    * Denne metoden leser kordinatene til de forskjellige kommunene og lagrer dem i en Map, som så blir returnert.
    * */
    fun loadCoordinates(): Map<Int, Pair<Double, Double>> {
        val coordinateMap = mutableMapOf<Int, Pair<Double, Double>>()
        try {
            context.assets.open("municipalities.csv").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        val parts = line.split("\t")
                        if (parts.size >= 4) {
                            val id = parts[0].toInt()
                            val lat = parts[2].toDouble()
                            val lon = parts[3].toDouble()

                            coordinateMap[id] = Pair(lat, lon)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading coordinates: ${e.message}")
            e.printStackTrace()
        }
        return coordinateMap
    }
}