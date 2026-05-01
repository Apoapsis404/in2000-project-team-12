package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            plant.name,
            style = MaterialTheme.typography.titleMedium
            )
        Icon(
            imageVector = Icons.Default.Nature,
            contentDescription = plant.category,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = "Optimal range: ${plant.optimal_temp_min}°C - ${plant.optimal_temp_max}°C"
        )
        Text("Sol: afsaff")
        Text("Fukt: sakfjaksfj")
    }
}

@Preview
@Composable
fun PreviewPlantCard(){
    PlantCard(plant = Plant(0, "Epletre", "Tre", 10, 20, 0, 30))
}