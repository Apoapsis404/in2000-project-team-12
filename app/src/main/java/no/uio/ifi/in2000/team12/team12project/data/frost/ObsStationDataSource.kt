package no.uio.ifi.in2000.team12.team12project.data.frost

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import no.uio.ifi.in2000.team12.team12project.data.exceptions.BadRequestException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.ServerErrorException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.ServiceBusyException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.UnauthorizedException
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStationIdResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStationResponse

private const val TAG = "ObsStationDataSource"

/**
 * DataSource for fetching observation stations from the MET Frost API.
 * 
 * Documentation: https://frost.met.no/howto.html
 */
class ObsStationDataSource {

    private companion object {
        // Frost API v0 endpoint for sources
        const val URL_V0 = "https://frost.met.no/sources/v0.jsonld"

        // Default credentials for the Frost API. 
        // Note: In a production app, these should be stored securely (e.g., BuildConfig or EncryptedSharedPreferences)
        const val DEFAULT_CLIENT_ID = "7dcf024d-0589-4aa3-afa7-79bc4904d7e2"
        const val DEFAULT_CLIENT_SECRET = "58727131-5656-406b-84f7-4838513e45f0"
    }

    /**
     * Fetches the nearest observation stations to a given coordinate.
     * 
     * ref: https://frost-rc.met.no/api/v1/obs/ranked/get?incobs=false&nearest={"maxdist":5,"maxcount":5,"points":[{"lon":10.75,"lat":59.91}]}
     *
     * @param latitude The latitude of the point.
     * @param longitude The longitude of the point.
     * @param maxCount Maximum number of stations to return (default is 5).
     * @param clientId The client ID for authentication (uses default if not provided).
     * @param clientSecret The client secret for authentication (uses default if not provided).
     * @return A list of [ObsStation]s.
     * @throws BadRequestException if the request is malformed (400).
     * @throws UnauthorizedException if authentication fails (401).
     * @throws NoDataException if no stations are found (404).
     * @throws ServiceBusyException if the service is busy (429/503).
     * @throws ServerErrorException if a server error occurs (500).
     */
    suspend fun getNearestStation(
        latitude: Double,
        longitude: Double,
        maxCount: Int = 5,
        clientId: String = DEFAULT_CLIENT_ID,
        clientSecret: String = DEFAULT_CLIENT_SECRET
    ): List<ObsStation> {
        val nearestString = "nearest(POINT ($longitude $latitude))"
        val fieldsString = "id,name,geometry"

        val response: HttpResponse = KtorClient.client.get(URL_V0) {
            basicAuth(clientId, clientSecret)
            url {
                parameters.append("geometry", nearestString)
                parameters.append("nearestmaxcount", maxCount.toString())
                parameters.append("fields", fieldsString)
            }
        }

        return handleResponse<ObsStationResponse>(response).data
    }

    /**
     * Fetches all MET.NO sensor system stations in Norway.
     *
     * @return A list of station IDs.
     */
    suspend fun getMetStations(): List<String> {
        val response: HttpResponse = KtorClient.client.get(URL_V0) {
            basicAuth(DEFAULT_CLIENT_ID, DEFAULT_CLIENT_SECRET)
            url {
                parameters.append("country", "norge")
                parameters.append("types", "SensorSystem")
                parameters.append("stationholder", "MET.NO")
                parameters.append("fields", "id")
            }
        }

        return handleResponse<ObsStationIdResponse>(response).data.map { it.id }
    }

    /**
     * Helper function to handle the [HttpResponse] and throw appropriate exceptions.
     */
    private suspend inline fun <reified T> handleResponse(response: HttpResponse): T {
        return when (response.status.value) {
            in 200..299 -> response.body<T>()
            400 -> throw BadRequestException("Invalid parameter value or malformed request")
            401 -> throw UnauthorizedException("Unauthorized client ID")
            404 -> throw NoDataException("No data found")
            429, 503 -> throw ServiceBusyException("Frost API is currently busy or rate-limited")
            500 -> throw ServerErrorException("Frost API encountered a server error")
            else -> {
                Log.e(TAG, "Frost API Error: ${response.status.value} - ${response.status.description}")
                throw Exception("Frost API encountered an unexpected error: ${response.status}")
            }
        }
    }
}
