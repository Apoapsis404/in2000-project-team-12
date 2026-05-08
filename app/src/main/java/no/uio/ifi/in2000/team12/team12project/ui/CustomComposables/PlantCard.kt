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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.sqlite.throwSQLiteException
import no.uio.ifi.in2000.team12.team12project.model.Plants.Plant

@Composable
fun PlantCard(
    plant: Plant,
    modifier: Modifier = Modifier
){
    Column(
        modifier = Modifier
            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
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
                "Ideal temp",
                "${plant.optimal_temp_min.toInt()}°C - ${plant.optimal_temp_max.toInt()}°C",
            )
            PlantInfoRow(
                "Tolerance range: ",
                "${plant.temp_min.toInt()}°C - ${plant.temp_max.toInt()}°C"
            )
            PlantInfoRow(
                "Humidity:",
                "${plant.humidity_min.toInt()}? - ${plant.humidity_max.toInt()}?"
            )
            PlantInfoRow(
                "Sunshine:",
                "${plant.sunshine_min.toInt()}? - ${plant.sunshine_max.toInt()}?"
            )
            PlantInfoRow(
                "Precipitation:",
                "${plant.precipitation_min.toInt()}mm - ${plant.precipitation_max.toInt()}mm"
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
        modifier = Modifier
        //    .fillMaxWidth(),
                ,
        //verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            lineHeight = 6.sp
        )
        //Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            lineHeight = 8.sp

        )
    }
}

fun CategoryIcon(category: String): ImageVector{
    when(category) {
        "Grain" -> return Icons.Default.Grain
        "Fodder" -> return Icons.Default.Grass
        "Legume" -> return Icons.Default.Egg
        "Root" -> return Icons.Default.Nature
        "Vegetable" -> return Icons.Default.Grass
        "Fruit" -> return Icons.Default.Nature
        "Nut" -> return Icons.Default.SportsRugby
        "Oilseed" -> return Icons.Default.WaterDrop
        "Industrial" -> return Icons.Default.Agriculture
        else -> return Icons.Default.Nature
    }
}

@Preview
@Composable
fun PreviewPlantCard(){
    PlantCard(plant = Plant(0, "Epletre", "Tre", 10f, 20f, 0f, 30f, 0f, 100f, 0f, 30f, 0f, 10f))
}