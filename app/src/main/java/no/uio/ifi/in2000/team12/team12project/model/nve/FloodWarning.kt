package no.uio.ifi.in2000.team12.team12project.model.nve
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FloodWarning(
    @SerialName("EventId")
    val eventId: Int,           //id for hendelsen
    @SerialName("ActivityLevel")
    val activityLevel: Int,     //faregrad
    @SerialName("DangerTypeName")
    val dangerTypeName: String,
    @SerialName("ValidFrom")
    val validFrom: String,
    @SerialName("ValidTo")
    val validTo: String,
    @SerialName("MunicipalityList")
    val municipalityList: List<Municipality>        //Liste over komuner som påvirkes av varselet
)

