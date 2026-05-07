package no.uio.ifi.in2000.team12.team12project.model.nve
import kotlinx.serialization.Serializable


@Serializable
data class Municipality(
    val id: Int,
    val name: String,
    val lat: Double? = null,   //sendes ikke inn med kordinater så må kunne være null
    val lon: Double? = null
)
