package no.uio.ifi.in2000.team12.team12project.data.nve

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.nve.FloodWarning

class NVEDataSource{
    val url: String = "https://api01.nve.no/hydrology/forecast/flood/v1.0.10/Warning/All/1"     //legge inn data for en dato hvis det ikke er noen varseler med /startdato på slutten

    suspend fun getFloodWarning(): List<FloodWarning> {
        val response: HttpResponse = KtorClient.client.get(url)
        val responseBodyString = response.bodyAsText()

        return if (response.status.value in 200..299) {
            Json { ignoreUnknownKeys = true }
                .decodeFromString<List<FloodWarning>>(responseBodyString)
        } else {
            throw Exception("Error getting flood warnings: ${response.status}")
        }
    }
}