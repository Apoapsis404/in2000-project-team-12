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
import kotlin.collections.map
import android.content.Context
import no.uio.ifi.in2000.team12.team12project.ui.home.getNameandAdress

class NVEDataSource(val context: Context){
    val url: String = "https://api01.nve.no/hydrology/forecast/flood/v1.0.10/api/Warning/1/2016-01-01/2026-01-01"     //legge inn data for en dato hvis det ikke er noen varseler med /startdato på slutten

    suspend fun getFloodWarning(): List<FloodWarning> {
        val response: HttpResponse = KtorClient.client.get(url)
        val responseBodyString = response.bodyAsText()

        if (response.status.value in 200..299) {
            val allWarnings = Json { ignoreUnknownKeys = true }
                .decodeFromString<List<FloodWarning>>(responseBodyString)


            return withContext(Dispatchers.Default) {
                val coordinateMap = loadCoordinates()

                // vil sortere etter de som har hatt flom med farenivå som 4 eller mer hvertfall 8 ganger. Den henter bare fra siste 15 år
                val relevantEventIds = allWarnings
                    .filter { it.ActivityLevel >= 4 }
                    .groupBy { it.EventId }
                    .filter { it.value.size >= 8 }
                    .keys

                val filtered = allWarnings.filter { it.EventId in relevantEventIds }
                println("filtered warnings: ${filtered.size}")

                filtered.map { warning ->
                        warning.copy(
                            MunicipalityList = warning.MunicipalityList.mapNotNull { municipality ->
                                val coordinates = coordinateMap[municipality.Id]
                                coordinates?.let {(lat, lon) ->
                                    municipality.copy(lat = lat, lon = lon)
                                }
                            }
                        )
                }
            }
        } else {
            throw Exception("Error getting flood warnings: ${response.status}")
        }
    }

    fun loadCoordinates(): Map<Int, Pair<Double, Double>> {
        val coordinateMap = mutableMapOf<Int, Pair<Double, Double>>()
        try {
            val inputStream = context.assets.open("kommuner.csv")
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.forEachLine { line ->
                    val part = line.split("\t")
                    if (part.size >= 4) {
                        try {
                            val id = part[0].toInt()
                            val name = part[1].trim()
                            val lat = part[2].toDouble()
                            val lon = part[3].toDouble()

                            coordinateMap[id] = Pair(lat, lon)
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return coordinateMap
    }
}