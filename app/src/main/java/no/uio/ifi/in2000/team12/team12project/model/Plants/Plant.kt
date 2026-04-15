package no.uio.ifi.in2000.team12.team12project.model.Plants

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: Int,
    val name: String,
    val category: String,
    val optimal_temp_min: Int,
    val optimal_temp_max: Int,
    val temp_min: Int,
    val temp_max: Int
)
