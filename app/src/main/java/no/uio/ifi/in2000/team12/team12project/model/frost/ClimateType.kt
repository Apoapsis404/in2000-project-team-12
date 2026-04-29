package no.uio.ifi.in2000.team12.team12project.model.frost

enum class ClimateType(val elementId: String, val unit: String, val description: String, val label: String) {
    MEAN_AIR_TEMP("mean(air_temperature P1M)", "°C", "Mean air temperature", "Mean air temperature"),
    MEAN_WIND_SPEED("mean(max(wind_speed P1D) P1M)", "m/s", "Mean wind speed", "Mean wind speed"),
    RELATIVE_HUMIDITY("mean(relative_humidity P1M)", "%", "Mean relative humidity", "Mean relative humidity"),
    PRECIPITATION("best_estimate_sum(precipitation_amount P1M)", "mm", "Total precipitation", "Total precipitation"),
    DURATION_OF_SUN("sum(duration_of_sunshine P1M)", "h", "Duration of sunshine", "Duration of sunshine"),;

    override fun toString(): String = label
}