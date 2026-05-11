package no.uio.ifi.in2000.team12.team12project.model.plants

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Plant(
    val id: Int,
    val name: String,
    val category: String,
    @SerialName("optimal_temp_min")
    val optimalTempMin: Float,
    @SerialName("optimal_temp_max")
    val optimalTempMax: Float,
    @SerialName("temp_min")
    val tempMin: Float,
    @SerialName("temp_max")
    val tempMax: Float,
    @SerialName("humidity_min")
    val humidityMin: Float,
    @SerialName("humidity_max")
    val humidityMax: Float,
    @SerialName("sunshine_min")
    val sunshineMin: Float,
    @SerialName("sunshine_max")
    val sunshineMax: Float,
    @SerialName("precipitation_min")
    val precipitationMin: Float,
    @SerialName("precipitation_max")
    val precipitationMax: Float
)
