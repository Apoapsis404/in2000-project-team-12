package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.Symbol
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team12.team12project.R

@Composable
fun InfoPopup(
    currentScreen: String
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (currentScreen == "home") {
            // INFO SCREEN FOR MAP
            Text("Information about the home screen:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // Symbolene på kartet
            Symbol(painterResource(R.drawable.outline_rainy_24), "Area prone to torrential rain")
            Symbol(painterResource(R.drawable.baseline_air_24), "Area prone to extreme wind")
            Symbol(painterResource(R.drawable.baseline_water_24), "Area prone to flooding. Area has had flood warnings of level 4 more that 8 times during the last 10 years")

            Spacer(modifier = Modifier.height(10.dp))

            //Knapper
            Symbol(Icons.Outlined.DataThresholding, "View climate graph for the selected location")
            Symbol(Icons.Default.FavoriteBorder, "Add markers location to favorites")
            Symbol(Icons.Default.MyLocation, "Center map to your current location")
            Symbol(Icons.Outlined.AddCircleOutline, "Zoom in on the map")
            Symbol(Icons.Outlined.AddCircleOutline, "Zoom out of the map")

        } else if (currentScreen == "graph") {
            // INFO SCREEN FOR GRAPH
            Text("Information about the graph screen:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Symbol(Icons.Outlined.Map, "Return to the map") // eller home screen
            Text("The graph displays the choosen climate varaible during the last 10 years for the selected month. \nThe graph will start with displaying temperature data for the month of January. " +
                    "\n\nThe button 'Plant prognosis' will give you a pop-up of plants that can be cultivated during the selected month. The slider chooses which year into the future you want to see the prediction for.", fontSize = 14.sp, modifier = Modifier.padding(8.dp))

        } else if (currentScreen == "favorites") {
            Text("The favoritte")
        }
    }
}
@Composable
fun Symbol(icon: ImageVector, text: String) {
    Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(8.dp)
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp)
    }
}

@Composable
fun Symbol(painter: Painter, text: String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ){
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp)
    }
}