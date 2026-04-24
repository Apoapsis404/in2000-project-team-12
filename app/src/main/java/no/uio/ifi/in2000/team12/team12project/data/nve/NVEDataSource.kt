package no.uio.ifi.in2000.team12.team12project.data.nve

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.nve.FloodWarning
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.collections.map

class NVEDataSource{
    val url: String = "https://api01.nve.no/hydrology/forecast/flood/v1.0.10/Warning/All/1/2010-01-01"     //legge inn data for en dato hvis det ikke er noen varseler med /startdato på slutten

    suspend fun getFloodWarning(): List<FloodWarning> {
        val response: HttpResponse = KtorClient.client.get(url)
        val responseBodyString = response.bodyAsText()

        if (response.status.value in 200..299) {
            val allWarnings = Json { ignoreUnknownKeys = true }
                .decodeFromString<List<FloodWarning>>(responseBodyString)

            val coordinateMap = loadCoordinates()

            // vil sortere etter de som har hatt flom med farenivå som 2 eller mer hvertfall 2 ganger. Den henter bare fra siste 16 år
            val events = allWarnings.filter { it.ActivityLevel >= 3 }
                .groupBy { it.EventId }
                .mapValues { it.value.size }

            val relevantEventIds = events.filter { it.value >= 2 }.keys

            return allWarnings.filter { it.EventId in relevantEventIds }
                .map { warning ->
                    warning.copy(
                        MunicipalityList = warning.MunicipalityList.map { municipality ->
                            val coordinates = coordinateMap[municipality.Id]
                            if (coordinates != null) {
                                municipality.copy(lat = coordinates.first, lon = coordinates.second)
                            } else {
                                municipality
                            }
                        }
                    )
                }


        } else {
            throw Exception("Error getting flood warnings: ${response.status}")
        }
    }

    fun loadCoordinates() {
        val coordinateMap = mutableMapOf<Int, Pair<Double, Double>>()
        try {
            val inputStream = javaClass.getResourceAsStream("kommuner.csv")
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val part = line.split("\t")
                    if (part.size >= 4) {
                        val id = part[0].toInt()
                        val name = part[1]
                        val lat = part[2].toDouble()
                        val lon = part[3].toDouble()

                        coordinateMap[id] = Pair(lat, lon)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}