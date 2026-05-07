package no.uio.ifi.in2000.team12.team12project.data.frost

import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.data.exceptions.BadRequestException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.UnauthorizedException
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObsResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStationIdResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStationResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.Point

@Serializable
data class NearestParam(val maxdist: Double, val maxcount: Int, val points: List<Point>)

class ObsStationsDataSource {

    // ref: https://frost-rc.met.no/api/v1/obs/ranked/get?incobs=false&nearest={"maxdist":5,"maxcount":5,"points":[{"lon":10.75,"lat":59.91}]}
    val urlV1: String = "https://frost-rc.met.no/api/v1/obs/ranked/get"
    val urlV0: String = "https://frost.met.no/sources/v0.jsonld"
    val username: String = "7e3f32d8-a66e-45a7-b8be-14e67bfbd5d4"
    val password: String = "4f33a622-b2de-4ca1-8dd0-2941e8d33a4a"

    val clientId: String = "7dcf024d-0589-4aa3-afa7-79bc4904d7e2"
    val clientSecret: String = "58727131-5656-406b-84f7-4838513e45f0"


    suspend fun getNearestStation(latitude: Double, longitude: Double, maxDist: Double = 5.0, maxCount: Int = 5, clientId: String = this.clientId, clientSecret: String = this.clientSecret): List<ObsStation> {

        val nearestString = "nearest(POINT ($longitude $latitude))"
        val fieldsString = "id,name,masl,geometry"

        val response: HttpResponse = KtorClient.client.get(urlV0) {

            basicAuth(clientId, clientSecret)

            url {
                parameters.append("geometry", nearestString)
                parameters.append("nearestmaxcount", maxCount.toString())
                parameters.append("fields", fieldsString)
            }
        }

        return when (response.status.value) {
            in 200..299 -> {
                response.body<ObsStationResponse>().data
            }
            400 -> {
                throw BadRequestException(message = "Invalid parameter value or malformed request")
            }
            401 -> {
                throw UnauthorizedException("Unauthorized client ID")
            }
            404 -> {
                throw NoDataException("No data found")
            }
            else -> {
                throw Exception("Unknown error")
            }
        }
    }
    
    
    suspend fun getMetStations(): String {
        val response: HttpResponse = KtorClient.client.get(urlV0) {

            basicAuth(clientId, clientSecret)

            url {
                parameters.append("country", "norge")
                parameters.append("stationholder", "MET.NO")
                parameters.append("fields", "id")
            }
        }

        return when (response.status.value) {
            in 200..299 -> {
                response.body<ObsStationIdResponse>().data.joinToString(",") { it.id }
            }
            400 -> {
                throw BadRequestException(message = "Invalid parameter value or malformed request")
            }
            401 -> {
                throw UnauthorizedException("Unauthorized client ID")
            }
            404 -> {
                throw NoDataException("No data found")
            }
            else -> {
                throw Exception("Unknown error")
            }
        }
    }
}