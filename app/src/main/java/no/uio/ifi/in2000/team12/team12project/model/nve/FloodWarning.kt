package no.uio.ifi.in2000.team12.team12project.model.nve
import kotlinx.serialization.Serializable


@Serializable
data class FloodWarning(
    val eventId: Int,           //id for hendelsen
    val activityLevel: Int,     //faregrad
    val dangerTypeName: String,
    val validFrom: String,
    val validTo: String,
    val municipalityList: List<Municipality>        //Liste over komuner som påvirkes av varselet
)

