package no.uio.ifi.in2000.team12.team12project.model.plants

import kotlinx.serialization.Serializable

@Serializable
data class Plant(
    val id: Int,
    val name: String,
    val category: String,
    val optimalTempMin: Float,
    val optimalTempMax: Float,
    val tempMin: Float,
    val tempMax: Float,
    val humidityMin: Float,
    val humidityMax: Float,
    val sunshineMin: Float,
    val sunshineMax: Float,
    val precipitationMin: Float,
    val precipitationMax: Float
)
