package no.uio.ifi.in2000.team12.team12project.model.nve
import kotlinx.serialization.Serializable


@Serializable
data class Municipality(
    val Id: Int,
    val Name: String,
    val lat: Double,
    val lon: Double
)
