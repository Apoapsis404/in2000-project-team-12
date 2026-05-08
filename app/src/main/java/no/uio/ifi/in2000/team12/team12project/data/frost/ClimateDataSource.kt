package no.uio.ifi.in2000.team12.team12project.data.frost

import io.ktor.client.call.body
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
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObsResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.TimeSeriesResponse

class ClimateDataSource {
    private val urlV0: String = "https://frost.met.no/observations/v0.jsonld"
    private val timeSeriesUrl: String = "https://frost.met.no/observations/availableTimeSeries/v0.jsonld"

    val clientId: String = "7dcf024d-0589-4aa3-afa7-79bc4904d7e2"
    val clientSecret: String = "58727131-5656-406b-84f7-4838513e45f0"
    

    suspend fun getClimateTimeSeries(sourceId: String, elementId: String, fields: String = "validFrom"): String {
        val response: HttpResponse = KtorClient.client.get(timeSeriesUrl) {
            basicAuth(clientId, clientSecret)

            url {
                parameters.append("sources", sourceId)
                parameters.append("elements", elementId)
                parameters.append("fields", fields)
            }
        }

        println(response.toString())

        return when (response.status.value) {
            in 200..299 -> {
                val timeSeriesResponse = response.body<TimeSeriesResponse>()
                timeSeriesResponse.data.firstOrNull()?.validFrom ?: ""
            }

            400 -> {
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
                throw Exception("Error getting climate data: ${response.status}")
            }
        }
    }

    suspend fun getClimateData(sources: String, elementIds: String, referenceTime: String, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<ClimateObs> {
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
                response.body<ClimateObsResponse>().data
            }
            400 -> {
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
                throw Exception("Error getting climate data: ${response.status}")
            }
        }
    }


}
