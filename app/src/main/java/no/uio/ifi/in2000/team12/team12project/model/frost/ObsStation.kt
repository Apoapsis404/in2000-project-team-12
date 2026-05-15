package no.uio.ifi.in2000.team12.team12project.model.frost

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ObsStation(
    val id: String,
    val name: String = "",
    val geometry: Geometry,
    val distance: Double = 0.0,
)

@Serializable
data class ObsStationResponse(
    val data: List<ObsStation>
)

@Serializable
data class Geometry(
    @SerialName("@type")
    val type: String,
    val coordinates: List<Double>,
    val nearest: Boolean
)

@Serializable
data class ObsStationId(
    val id: String
)

@Serializable
data class ObsStationIdResponse (
    val data: List<ObsStationId>
)
