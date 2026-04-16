package no.uio.ifi.in2000.team12.team12project.data.frost

import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.exceptions.BadRequestException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.UnauthorizedException
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObsResponse

class WeatherDataSource {
    private val urlV1: String = "https://frost-rc.met.no/api/1/obs/ranked/get?"
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
        //println(response.toString())
        val responseBodyString = response.bodyAsText()
        //println(response.status.value)
        return when (response.status.value) {
            in 200..299 -> {
                println("DEBUG: Success branch")
                Json { ignoreUnknownKeys = true }.decodeFromString<WeatherObsResponse>(
                    responseBodyString
                ).data
            }
            400 -> {
                println("DEBUG: Throwing BadRequestException")
                throw BadRequestException("Invalid parameter value or malformed request")
            }
            401 -> {
                throw UnauthorizedException("Unauthorized client ID")
            }
            404 -> {
                throw NoDataException("No data found")
            }
            412 -> {
                throw NoAvailableTimeSeriesException("No available time series")
            }
            else -> {
                println("DEBUG: Throwing generic Exception")
                throw Exception("Error getting weather data: ${response.status}")
            }
        }
    }


}