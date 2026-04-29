package no.uio.ifi.in2000.team12.team12project.model.frost

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class ClimateType(
    val elementId: String,
    val unit: String,
    val description: String,
    val label: String,
    val icon: ImageVector
) {
    MEAN_AIR_TEMP(
        "mean(air_temperature P1M)",
        "°C",
        "Mean air temperature",
        "Mean air temperature",
        Icons.Default.Thermostat
    ),
    MEAN_WIND_SPEED(
        "mean(max(wind_speed P1D) P1M)",
        "m/s",
        "Mean wind speed",
        "Mean wind speed",
        Icons.Default.Air
    ),
    RELATIVE_HUMIDITY(
        "mean(relative_humidity P1M)",
        "%",
        "Mean relative humidity",
        "Mean relative humidity",
        Icons.Default.WaterDrop
    ),
    PRECIPITATION(
        "best_estimate_sum(precipitation_amount P1M)",
        "mm",
        "Total precipitation",
        "Total precipitation",
        Icons.Default.Umbrella
    ),
    DURATION_OF_SUN(
        "sum(duration_of_sunshine P1M)",
        "h",
        "Duration of sunshine",
        "Duration of sunshine",
        Icons.Default.WbSunny
    );

    override fun toString(): String = label
}
