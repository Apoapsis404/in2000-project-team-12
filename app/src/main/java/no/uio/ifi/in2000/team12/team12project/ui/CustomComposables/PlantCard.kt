package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.SportsRugby
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.sqlite.throwSQLiteException
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.Plants.Plant

@Composable
fun PlantCard(
    plant: Plant,
){
    Column(
        modifier = Modifier
            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            plant.name,
            style = MaterialTheme.typography.titleMedium
            )
        Icon(
            imageVector = CategoryIcon(plant.category),
            contentDescription = plant.category,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(80.dp)
        )
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(2.dp)
        ) {
            PlantInfoRow(
                stringResource(R.string.plant_card_ideal_temp),
                stringResource(
                    R.string.temp_range,
                    plant.optimal_temp_min.toInt(),
                    plant.optimal_temp_max.toInt()
                ),
            )
            PlantInfoRow(
                stringResource(R.string.plant_card_tolerance_range),
                stringResource(
                    R.string.temp_range,
                    plant.temp_min.toInt(),
                    plant.temp_max.toInt()
                )
            )
            PlantInfoRow(
                stringResource(R.string.plant_card_humidity),
                stringResource(
                    R.string.humidity_range,
                    plant.humidity_min.toInt(),
                    plant.humidity_max.toInt()
                )
            )
            PlantInfoRow(
                stringResource(R.string.plant_card_sunshine),
                stringResource(
                    R.string.sunshine_range,
                    plant.sunshine_min.toInt(),
                    plant.sunshine_max.toInt()
                )
            )
            PlantInfoRow(
                stringResource(R.string.plant_card_precipitation),
                stringResource(
                    R.string.precipitation_range,
                    plant.precipitation_min.toInt() * 30, // mm/d to mm/month
                    plant.precipitation_max.toInt() * 30
                )
            )
        }

    }
}

@Composable
fun PlantInfoRow(
    text: String,
    value: String,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            lineHeight = 6.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            lineHeight = 8.sp

        )
    }
}

fun CategoryIcon(category: String): ImageVector{
    return when(category) {
        "Grain" -> Icons.Default.Grain
        "Fodder" -> Icons.Default.Grass
        "Legume" -> Icons.Default.Egg
        "Root" -> Icons.Default.Nature
        "Vegetable" -> Icons.Default.Grass
        "Fruit" -> Icons.Default.Nature
        "Nut" -> Icons.Default.SportsRugby
        "Oilseed" -> Icons.Default.WaterDrop
        "Fiber" -> Icons.Default.LocalFlorist
        "Industrial" -> Icons.Default.Agriculture
        else -> Icons.Default.Nature
    }
}

@Preview
@Composable
fun PreviewPlantCard(){
    PlantCard(plant = Plant(0, "Epletre", "Tre", 10f, 20f, 0f, 30f, 0f, 100f, 0f, 30f, 0f, 10f))
}