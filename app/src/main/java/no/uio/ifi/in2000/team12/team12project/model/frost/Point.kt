package no.uio.ifi.in2000.team12.team12project.model.frost

import kotlinx.serialization.Serializable

@Serializable
data class Point(
    val lon: Double,
    val lat: Double
)
