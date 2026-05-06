package no.uio.ifi.in2000.team12.team12project.model.Plants

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: Int,
    val name: String,
    val category: String,
    val optimal_temp_min: Float,
    val optimal_temp_max: Float,
    val temp_min: Float,
    val temp_max: Float,
    val humidity_min: Float,
    val humidity_max: Float,
    val sunshine_min: Float,
    val sunshine_max: Float,
    val precipitation_min: Float,
    val precipitation_max: Float
)
