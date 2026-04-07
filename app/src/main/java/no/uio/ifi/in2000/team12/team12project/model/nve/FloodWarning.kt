package no.uio.ifi.in2000.team12.team12project.model.nve

data class FloodWarning(
    val EventId: Int,           //id for hendelsen
    val ActivityLevel: Int,     //faregrad
    val DangerTypeName: String,
    val ValidFrom: String,
    val ValidTo: String,
    val MunicipalityList: List<Municipality>        //Liste over komuner som påvirkes av varselet
)

data class Municipality(
    val Id: Int,
    val Name: String
)
