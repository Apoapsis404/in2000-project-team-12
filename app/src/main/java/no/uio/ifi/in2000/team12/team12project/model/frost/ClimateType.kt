package no.uio.ifi.in2000.team12.team12project.model.frost

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import no.uio.ifi.in2000.team12.team12project.R

enum class ClimateType(
    val elementId: String,
    val unit: String,
    @StringRes val description: Int,
    @StringRes val label: Int,
    val icon: ImageVector
) {
    MEAN_AIR_TEMP(
        "mean(air_temperature P1M)",
        "°C",
        R.string.temperature_description,
        R.string.temperature,
        Icons.Default.Thermostat
    ),
    RELATIVE_HUMIDITY(
        "mean(relative_humidity P1M)",
        "%",
        R.string.humidity_description,
        R.string.humidity,
        Icons.Default.WaterDrop
    ),
    PRECIPITATION(
        "best_estimate_sum(precipitation_amount P1M)",
        "mm",
        R.string.precipitation_description,
        R.string.precipitation,
        Icons.Default.Umbrella
    ),
    DURATION_OF_SUN(
        "sum(duration_of_sunshine P1M)",
        "h",
        R.string.sunshine_description,
        R.string.sunshine,
        Icons.Default.WbSunny
    );

    override fun toString(): String = label.toString()
}
