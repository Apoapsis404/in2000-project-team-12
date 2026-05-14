package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.ui.theme.humidityBlue
import no.uio.ifi.in2000.team12.team12project.ui.theme.precipiationBlue
import no.uio.ifi.in2000.team12.team12project.ui.theme.sunshineYellow

private const val MIN_TEMP = -30f
private const val MAX_TEMP = 40f
private const val MAX_PRECIP = 100f
private const val MAX_SUN = 400f

@Composable
fun GraphCard(
    modifier: Modifier = Modifier,
    climateType: ClimateType = ClimateType.MEAN_AIR_TEMP,
    meanValue: Double = 0.0,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true
) {
    val containerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        selected -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        selected -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onPrimary
    }

    Card(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        onClick = { if (enabled) onClick() },
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val title = climateType.label.split(" ").last().replaceFirstChar { it.uppercase() }

            Text(
                text = title,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Dim the visuals if not enabled
                val visualAlpha = if (enabled) 1f else 0.3f
                Box(modifier = Modifier.graphicsLayer { alpha = visualAlpha }) {
                    when (climateType) {
                        ClimateType.RELATIVE_HUMIDITY -> HumidityVisual(meanValue, contentColor)
                        ClimateType.MEAN_AIR_TEMP -> TemperatureVisual(meanValue, contentColor)
                        ClimateType.PRECIPITATION -> PrecipitationVisual(meanValue, contentColor)
                        ClimateType.DURATION_OF_SUN -> SunVisual(meanValue, contentColor)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (enabled) "${meanValue.toInt()}${climateType.unit}" else "--",
                    color = contentColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (enabled) climateType.description else "Data unavailable",
                    color = contentColor.copy(alpha = if (enabled) 0.7f else 0.4f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HumidityVisual(value: Double, baseColor: Color) {
    val progress = (value.toFloat() / 100f).coerceIn(0f, 1f)
    
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .height(55.dp)
                .width(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50))
                    .background(baseColor.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .clip(RoundedCornerShape(50))
                    .background(humidityBlue)
            )
        }
        Icon(
            Icons.Default.WaterDrop,
            contentDescription = "Humidity indicator",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun TemperatureVisual(value: Double, baseColor: Color) {
    val progress = ((value.toFloat() - MIN_TEMP) / (MAX_TEMP - MIN_TEMP)).coerceIn(0f, 1f)
    
    val accentColor = if (value >= 0) Color(0xFFFF5252) else Color(0xFF448AFF)

    Box(
        modifier = Modifier
            .height(65.dp)
            .width(20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(50))
                .background(baseColor.copy(0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .align(Alignment.BottomCenter)
                    .background(accentColor)
            )
        }
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
    }
}

@Composable
fun PrecipitationVisual(value: Double, baseColor: Color) {
    val progress = (value.toFloat() / MAX_PRECIP).coerceIn(0f, 1f)

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(baseColor.copy(0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .align(Alignment.BottomCenter)
                    .background(precipiationBlue)
            )
        }
        Icon(
            Icons.Default.Umbrella,
            contentDescription = "Precipitation indicator",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun SunVisual(value: Double, baseColor: Color) {
    val progress = (value.toFloat() / MAX_SUN).coerceIn(0f, 1f)

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(55.dp)) {
            drawCircle(
                color = baseColor.copy(alpha = 0.15f),
                style = Stroke(width = 4f)
            )
            drawArc(
                color = sunshineYellow,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )
        }
        Icon(
            Icons.Default.WbSunny,
            contentDescription = "Sun duration indicator",
            tint = sunshineYellow,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GraphCardPreview() {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            GraphCard(
                modifier = Modifier.weight(1f),
                climateType = ClimateType.RELATIVE_HUMIDITY,
                meanValue = 39.0,
                onClick = {}
            )
            GraphCard(
                modifier = Modifier.weight(1f),
                climateType = ClimateType.MEAN_AIR_TEMP,
                meanValue = 0.0,
                onClick = {}
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            GraphCard(
                modifier = Modifier.weight(1f),
                climateType = ClimateType.DURATION_OF_SUN,
                meanValue = 39.0,
                onClick = {}
            )
            GraphCard(
                modifier = Modifier.weight(1f),
                climateType = ClimateType.PRECIPITATION,
                meanValue = 22.0,
                onClick = {}
            )
        }
    }
}
