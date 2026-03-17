package no.uio.ifi.in2000.team12.team12project.data.frost

import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObsResponse

class WeatherDataSource {
    private val urlString: String = "https://frost-rc.met.no/api/1/obs/ranked/get?"
    private val urlV0: String = "https://frost.met.no/observations/v0.jsonld"

    val clientId: String = "7dcf024d-0589-4aa3-afa7-79bc4904d7e2"
    val clientSecret: String = "58727131-5656-406b-84f7-4838513e45f0"

    suspend fun getWeatherData(sources: String, elementIds: String, referenceTime: String, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<WeatherObs> {
        val response: HttpResponse = KtorClient.client.get(urlV0) {
            basicAuth(clientId, clientSecret)

            url {
                parameters.append("sources", sources)
                parameters.append("referencetime", referenceTime)
                parameters.append("elements", elementIds)
                parameters.append("fields", fields)
            }
        }
        println(response.toString())
        val responseBodyString = response.bodyAsText()

        //println(responseBodyString)
        return if (response.status.value in 200..299) {
            Json { ignoreUnknownKeys = true }.decodeFromString<WeatherObsResponse>(responseBodyString).data
        } else {
            throw Exception("Error getting nearest station: ${response.status}")
        }
    }
}