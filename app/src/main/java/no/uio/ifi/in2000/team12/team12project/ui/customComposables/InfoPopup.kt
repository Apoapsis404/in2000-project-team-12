package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LineAxis
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.FilterList
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.ui.theme.graphInfoGreen

@Composable
fun InfoPopup(
    currentScreen: String
){
    val verticalScrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(verticalScrollState)
    ) {
        when (currentScreen) {
            "home" -> {
                // INFO SCREEN FOR MAP
                Text(stringResource(R.string.info_home_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                SymbolBulletPoint(painterResource(R.drawable.outline_rainy_24), stringResource(R.string.info_home_rain))
                SymbolBulletPoint(painterResource(R.drawable.baseline_air_24), stringResource(R.string.info_home_wind))
                SymbolBulletPoint(painterResource(R.drawable.baseline_water_24), stringResource(R.string.info_home_flood))
                Spacer(modifier = Modifier.height(10.dp))
                SymbolBulletPoint(Icons.Outlined.DataThresholding, stringResource(R.string.info_home_graph))
                SymbolBulletPoint(Icons.Default.FavoriteBorder, stringResource(R.string.info_home_favorites))
                SymbolBulletPoint(Icons.Default.MyLocation, stringResource(R.string.info_home_my_location))
                SymbolBulletPoint(Icons.Outlined.AddCircleOutline, stringResource(R.string.info_home_zoom_in))
                SymbolBulletPoint(Icons.Outlined.AddCircleOutline, stringResource(R.string.info_home_zoom_out))
                SymbolBulletPoint(Icons.Outlined.FilterList, stringResource(R.string.filter_hazards))
            }

            "graph" -> {
                // INFO SCREEN FOR GRAPH
                Text(stringResource(R.string.info_graph_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                SymbolBulletPoint(Icons.Outlined.Map, stringResource(R.string.info_graph_map))
                SymbolBulletPoint(Icons.Filled.LineAxis, stringResource(R.string.info_graph_blue_line),
                    Color.Blue)
                SymbolBulletPoint(Icons.Filled.LineAxis, stringResource(R.string.info_graph_regression),
                    graphInfoGreen
                )
                SymbolBulletPoint(Icons.Filled.Square, stringResource(R.string.info_graph_climate_type_boxes))
                //plant-prognosis
                Text(stringResource(R.string.info_graph_plant_sub_title), textAlign = TextAlign.Center)
                SymbolBulletPoint(Icons.Filled.DragHandle, stringResource(R.string.info_graph_plant_slider))
                SymbolBulletPoint(Icons.Filled.Grass, stringResource(R.string.info_graph_plant_plant_row))
            }

            "favorites" -> {
                // INFO SCREEN FOR FAVORITES
                Text(stringResource(R.string.info_favorite_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                SymbolBulletPoint(Icons.Filled.DataThresholding, stringResource(R.string.info_favorite_graph_symbol))
                SymbolBulletPoint(Icons.Filled.LocationOn, stringResource(R.string.info_favorite_location_symbol))
                SymbolBulletPoint(Icons.Filled.Square, stringResource(R.string.info_favorite_card))
            }

            "settings" -> {
                //INFO SCREEN FOR SETTINGS
                Text(stringResource(R.string.info_settings_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                SymbolBulletPoint(Icons.Filled.ToggleOn, stringResource(R.string.info_settings_toggle_theme))
                SymbolBulletPoint(Icons.Filled.ArrowDropDown, stringResource(R.string.info_settings_language))
            }

            "notes" -> {
                //INFO SCREEN FOR NOTES
                Text(stringResource(R.string.info_notes_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                SymbolBulletPoint(Icons.Filled.Square, stringResource(R.string.info_notes_edit_delete))
                SymbolBulletPoint(Icons.Filled.Circle, stringResource(R.string.info_notes_add_note))
            }
        }
    }
}
@Composable
fun SymbolBulletPoint(icon: ImageVector, text: String, tint: Color = MaterialTheme.colorScheme.primary) {
    Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(8.dp)
    ){
        Icon(
            imageVector = icon,
            contentDescription = icon.name,
            modifier = Modifier.size(25.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp)
    }
}

@Composable
fun SymbolBulletPoint(painter: Painter, text: String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ){
        Icon(
            painter = painter,
            contentDescription = painter.toString(),
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp)
    }
}