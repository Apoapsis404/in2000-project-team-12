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
import androidx.compose.material.icons.filled.Air
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

@Composable
fun GraphCard(
    modifier: Modifier = Modifier,
    climateType: ClimateType = ClimateType.MEAN_AIR_TEMP,
    meanValue: Double = 0.0,
    onClick: () -> Unit,
    selected : Boolean = false
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.primary
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title at the top
            Text(
                text = climateType.label.split(" ").last().replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

            // Visual representation in the middle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (climateType) {
                    ClimateType.RELATIVE_HUMIDITY -> HumidityVisual(meanValue)
                    ClimateType.MEAN_AIR_TEMP -> TemperatureVisual(meanValue)
                    //ClimateType.MEAN_WIND_SPEED -> WindVisual(meanValue)
                    ClimateType.PRECIPITATION -> PrecipitationVisual(meanValue)
                    ClimateType.DURATION_OF_SUN -> SunVisual(meanValue)
                }
            }

            // Value at the bottom
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${meanValue.toInt()}${climateType.unit}",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = climateType.description,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
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
fun HumidityVisual(value: Double) {
    val progress = (value.toFloat() / 100f).coerceIn(0f, 1f)
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    
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
                    .background(onPrimary.copy(alpha = 0.15f))
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
            null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun TemperatureVisual(value: Double) {
    val minTemp = -30f
    val maxTemp = 40f
    val progress = ((value.toFloat() - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f)
    val accentColor = if (value >= 0) Color.Red else Color.Blue  //MaterialTheme.colorScheme.tertiary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

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
                .background(onPrimary.copy(0.2f))
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
fun WindVisual(value: Double) {
    val maxWind = 25f
    val progress = (value.toFloat() / maxWind).coerceIn(0f, 1f)
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(55.dp)) {
            drawArc(
                color = onPrimary.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            drawArc(
                color = secondaryColor,
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        }
        Icon(
            Icons.Default.Air,
            null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PrecipitationVisual(value: Double) {
    val maxPrecip = 100f
    val progress = (value.toFloat() / maxPrecip).coerceIn(0f, 1f)
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(onPrimary.copy(0.15f))
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
            null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun SunVisual(value: Double) {
    val maxSun = 400f
    val progress = (value.toFloat() / maxSun).coerceIn(0f, 1f)
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(55.dp)) {
            drawCircle(
                color = onPrimary.copy(alpha = 0.15f),
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
            null,
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
                meanValue = -0.0,
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
