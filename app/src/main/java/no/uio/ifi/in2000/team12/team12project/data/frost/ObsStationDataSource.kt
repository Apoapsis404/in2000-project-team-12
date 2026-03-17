package no.uio.ifi.in2000.team12.team12project.data.frost

import android.util.Log
import androidx.compose.ui.res.stringResource
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.*
import io.ktor.client.request.*
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStationResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.Point
import org.json.JSONObject

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


    suspend fun getNearestStation(latitude: Double, longitude: Double, maxDist: Double = 5.0, maxCount: Int = 5): List<ObsStation> {

        val nearestString: String = "nearest(POINT ($longitude $latitude))"
        val fieldsString: String = "id,name,masl,geometry"

        val response: HttpResponse = KtorClient.client.get(urlV0) {

            basicAuth(clientId, clientSecret)

            url {
                parameters.append("geometry", nearestString)
                parameters.append("nearestmaxcount", maxCount.toString())
                parameters.append("fields", fieldsString)
            }
        }

        println(response.toString())

        val responseBodyString = response.bodyAsText()

        //println(responseBodyString)
        return if (response.status.value in 200..299) {
            //response.body<ObsStation>()
            Json { ignoreUnknownKeys = true }.decodeFromString<ObsStationResponse>(responseBodyString).data
        } else {
            throw Exception("Error getting nearest station: ${response.status}")
        }
    }

}