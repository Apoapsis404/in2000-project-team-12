package no.uio.ifi.in2000.team12.team12project.model.frost

import kotlinx.serialization.Serializable

@Serializable
data class WeatherObs(
    val sourceId: String,
    val referenceTime: String,
    val observations: List<Observation>
)

@Serializable
data class Observation(
    val elementId: String,
    val value: Double,
    val unit: String
)

@Serializable
data class WeatherObsResponse(
    val data: List<WeatherObs>
)