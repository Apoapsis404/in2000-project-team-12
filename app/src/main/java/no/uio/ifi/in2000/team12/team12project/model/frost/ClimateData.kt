package no.uio.ifi.in2000.team12.team12project.model.frost


data class ClimateData(
    val referenceTime: String,
    val climateObs: List<ClimateObs>,
    val climateType: ClimateType,
)
