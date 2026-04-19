package no.uio.ifi.in2000.team12.team12project.model.frost

enum class ClimateType(val elementId: String, val unit: String, val description: String, val label: String) {
    MEAN_AIR_TEMP("mean(air_temperature P1M)", "°C", "Mean air temperature", "Mean air temperature"),
    MEAN_WIND_SPEED("mean(max(wind_speed P1D) P1M)", "m/s", "Mean wind speed", "Mean wind speed"),;

    override fun toString(): String = label
}