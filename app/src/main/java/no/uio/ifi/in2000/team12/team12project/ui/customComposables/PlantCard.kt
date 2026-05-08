package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.plants.Plant
import androidx.sqlite.throwSQLiteException


@Composable
fun PlantCard(
    plant: Plant,
    //modifier: Modifier = Modifier
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
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
            )
        Icon(
            painter = categoryIcon(plant.category),
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
            lineHeight = 6.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            lineHeight = 8.sp,
            color = MaterialTheme.colorScheme.onPrimary

        )
    }
}

@Composable
fun categoryIcon(category: String): Painter {
    return when(category) {
        "Grain" -> painterResource(id = R.drawable.icon_grain)
        "Fodder" -> painterResource(id = R.drawable.icon_fodder)
        "Legume" -> painterResource(id = R.drawable.icon_legume)
        "Root" -> painterResource(id = R.drawable.icon_root)
        "Vegetable" -> painterResource(id = R.drawable.icons_vegetable)
        "Fruit" -> painterResource(id = R.drawable.icon_fruit)
        "Nut" -> painterResource(id = R.drawable.icon_nut)
        "Oilseed" -> painterResource(id = R.drawable.icon_oilseed)
        "Industrial" -> painterResource(id = R.drawable.icon_cannabis)
        "Herb" -> painterResource(id = R.drawable.icon_herb)
        "Berry" -> painterResource(id = R.drawable.icons_berry)
        else -> rememberVectorPainter(Icons.Default.Nature)
    }
}

@Preview
@Composable
fun PreviewPlantCard(){
    PlantCard(plant = Plant(0, "Epletre", "Tre", 10f, 20f, 0f, 30f, 0f, 100f, 0f, 30f, 0f, 10f))
}