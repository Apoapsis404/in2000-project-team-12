package no.uio.ifi.in2000.team12.team12project.model.frost

import kotlinx.serialization.Serializable

@Serializable
data class HazardEvent(
    val coordinates: List<Double>,
    val events: Int
)
