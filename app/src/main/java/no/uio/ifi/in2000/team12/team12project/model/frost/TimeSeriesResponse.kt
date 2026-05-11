package no.uio.ifi.in2000.team12.team12project.model.frost

import kotlinx.serialization.Serializable

@Serializable
data class TimeSeriesResponse(
    val data: List<TimeSeriesData>
)

@Serializable
data class TimeSeriesData(
    val validFrom: String? = null,
    val validTo: String? = null
)
