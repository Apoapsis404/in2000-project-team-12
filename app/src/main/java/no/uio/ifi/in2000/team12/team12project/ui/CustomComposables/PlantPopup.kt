package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.Plants.Plant
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenUiState
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel
import java.util.PriorityQueue

@Composable
fun PlantPopup(
    graphScreenViewModel: GraphScreenViewModel,
    uiState: GraphScreenUiState,
    modifier: Modifier = Modifier
){
    var plantPq = graphScreenViewModel.getBestPlants(uiState.predictedTemperature)
    Column(
       modifier =  Modifier
           .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.plant_popup_header))
        DropDown<Months>(
            uiState.month.toString(),
            onClick = {graphScreenViewModel.setMonth(it)},
 //           modifier = modifier.weight(1f)
        )
        Slider(
            value = uiState.yearSelected,
            valueRange = 2026f..2126f, // Hardcoded range for now
            steps = 100,
            onValueChange = {
                graphScreenViewModel.updateSelectedYear(it)
                //plantPq = graphScreenViewModel.getBestPlants(uiState.predictedTemperature)
                graphScreenViewModel.updatePredictedTemperature()
            } ,
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp)
                .fillMaxWidth(),

            )
        Text(
            "Selected year: ${uiState.yearSelected.toInt()}",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
                "predicted temp: ${uiState.predictedTemperature}",
        modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val plantCount = if (plantPq.size > 3) 3 else plantPq.size
            items(plantCount) {
                PlantCard(plantPq.poll()!!)
            }
        }
    }
}
