package no.uio.ifi.in2000.team12.team12project.data.frost

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import no.uio.ifi.in2000.team12.team12project.BuildConfig
import no.uio.ifi.in2000.team12.team12project.data.exceptions.BadRequestException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.ServerErrorException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.ServiceBusyException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.UnauthorizedException
import no.uio.ifi.in2000.team12.team12project.data.network.KtorClient
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObsResponse
import no.uio.ifi.in2000.team12.team12project.model.frost.TimeSeriesResponse

private const val TAG = "ClimateDataSource"

/**
 * DataSource for fetching climate observations and time series from the MET Frost API.
 * 
 * Documentation: https://frost.met.no/howto.html
 */
class ClimateDataSource {
    private companion object {
        const val URL_OBSERVATIONS = "https://frost.met.no/observations/v0.jsonld"
        const val URL_TIME_SERIES = "https://frost.met.no/observations/availableTimeSeries/v0.jsonld"
    }

    /**
     * Fetches the start date of a climate time series for a given source and element.
     *
     * @param sourceId The ID of the observation station.
     * @param elementId The ID of the climate element (e.g., mean air temperature).
     * @param fields Fields to return (default is "validFrom,validTo").
     * @return The validFrom date as a string, or an empty string if not found.
     * @throws BadRequestException if the request is malformed (400).
     * @throws UnauthorizedException if authentication fails (401).
     * @throws NoDataException if no data is found (404).
     * @throws NoAvailableTimeSeriesException if no time series is available (412).
     */
    suspend fun getClimateTimeSeries(
        sourceId: String,
        elementId: String,
        fields: String = "validFrom,validTo"
    ): String {
        val response: HttpResponse = KtorClient.client.get(URL_TIME_SERIES) {
            basicAuth(BuildConfig.FROST_CLIENT_ID, BuildConfig.FROST_CLIENT_SECRET)
            url {
                parameters.append("sources", sourceId)
                parameters.append("elements", elementId)
                parameters.append("fields", fields)
            }
        }

        val data = handleResponse<TimeSeriesResponse>(response).data.firstOrNull() ?: return ""
        val from = data.validFrom ?: return ""
        return if (data.validTo != null) {
            "$from,${data.validTo}"
        } else {
            from
        }
    }

    /**
     * Fetches climate observations for a given source, element, and time period.
     *
     * @param sources Comma-separated station IDs.
     * @param elementIds Comma-separated element IDs.
     * @param referenceTime The time period (e.g., "1981-01-01/2010-12-31").
     * @param fields Fields to return.
     * @return A list of [ClimateObs].
     * @throws BadRequestException if the request is malformed (400).
     * @throws UnauthorizedException if authentication fails (401).
     * @throws NoDataException if no data is found (404).
     */
    suspend fun getClimateData(
        sources: String,
        elementIds: String,
        referenceTime: String,
        fields: String = "sourceId,referenceTime,elementId,unit,value"
    ): List<ClimateObs> {
        val response: HttpResponse = KtorClient.client.get(URL_OBSERVATIONS) {
            basicAuth(BuildConfig.FROST_CLIENT_ID, BuildConfig.FROST_CLIENT_SECRET)
            url {
                parameters.append("sources", sources)
                parameters.append("referencetime", referenceTime)
                parameters.append("elements", elementIds)
                parameters.append("fields", fields)
            }
        }

        return handleResponse<ClimateObsResponse>(response).data
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
            412 -> throw NoAvailableTimeSeriesException("No available time series")
            429, 503 -> throw ServiceBusyException("Frost API is currently busy or rate-limited")
            500 -> throw ServerErrorException("Frost API encountered a server error")
            else -> {
                Log.e(TAG, "Frost API Error: ${response.status.value} - ${response.status.description}")
                throw Exception("Frost API encountered an unexpected error: ${response.status}")
            }
        }
    }
}
