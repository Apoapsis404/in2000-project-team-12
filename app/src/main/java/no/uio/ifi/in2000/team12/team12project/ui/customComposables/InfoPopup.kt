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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AddLocation
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.Symbol
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            Text("The Home Screen:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

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
            Text("The Graph Screen:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Symbol(Icons.Outlined.Map, "Return to the map") // eller home screen
//            Text("The graph displays the chosen climate variable for the selected month. " +
//                    "\nThe graph will start with displaying temperature data for the month of January. " +
//                    "\n\nThe button 'Plant prognosis' will give you a pop-up of plants that can be cultivated during the selected month. The slider chooses which year into the future you want to see the prediction for.", fontSize = 14.sp, modifier = Modifier.padding(8.dp))

            Symbol(Icons.Filled.Circle, "The blue line in the graph displays the data for the chosen climate type for the selected month for all years with data available.", Color.Blue)
            Symbol(Icons.Filled.Circle, "The green line graph displays a linear regression of the blue line.", Color.Green)
            Symbol(Icons.Filled.Square,"The climate boxes shows the mean data for the selected month over the years in the graph.")


            Text("Plant Prognosis", textAlign = TextAlign.Center)
            Symbol(Icons.Filled.DragHandle, "The slider allows you to select the year into the future you want to see the prediction for")
            Symbol(Icons.Filled.Grass, "The plants shows the top 3 plants that can be cultivated during the selected month and year, based on the regression in the graph.")

        } else if (currentScreen == "favorites") {
            Text("The Favorite Screen:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Symbol(Icons.Filled.DataThresholding, "Icon on card to navigate to the graph screen to show data on favorite location")
            Symbol(Icons.Filled.Square,"Click on card to edit or delete favorite")
        } else if (currentScreen == "settings") {
        Text(
            "The Settings Screen",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
            Symbol(Icons.Filled.ToggleOn, "Change between light/dark mode")
            Symbol(Icons.Filled.ArrowDropDown, "Change language")
    } else if (currentScreen == "notes") {
        Text("The Notes Screen:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Symbol(Icons.Filled.Square,"Click on card to edit or delete note")
        Symbol(Icons.Filled.Circle,"Click on 'add note' to add new note")

        }
    }
}
@Composable
fun Symbol(icon: ImageVector, text: String, tint: Color = MaterialTheme.colorScheme.primary) {
    Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(8.dp)
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(25.dp),
            tint = tint
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