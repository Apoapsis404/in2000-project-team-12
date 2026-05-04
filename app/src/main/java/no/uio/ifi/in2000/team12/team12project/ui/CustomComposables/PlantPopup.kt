package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val plantPq = graphScreenViewModel.getBestPlants(uiState.predictedTemperature)
    Column(
       modifier =  Modifier
           .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.plant_popup_header),
        )
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
                graphScreenViewModel.updatePredictedTemperature()
            } ,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            )
        Text(
            "Selected year: ${uiState.yearSelected.toInt()}",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
                "predicted temp: ${uiState.predictedTemperature}C",
        modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ){
            val plantCount = if (plantPq.size > 3) 3 else plantPq.size
            if(plantCount <= 0){
                Text("No plants found")
            }else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(plantCount) {
                        PlantCard(plantPq.poll()!!)
                    }
                }
            }
        }
    }
}


