package no.uio.ifi.in2000.team12.team12project.ui.customComposables

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.localizedName
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenUiState
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel

@Composable
fun PlantPopup(
    graphScreenViewModel: GraphScreenViewModel,
    uiState: GraphScreenUiState,
){
    val plantPq = graphScreenViewModel.getBestPlants(
        uiState.predictedTemperature,
        targetHumidity = uiState.predictedHumidity,
        targetSunshine = uiState.predictedSunshine,
        targetPrecipitation = uiState.predictedPrecipitation,
    )
    val verticalScrollState = rememberScrollState()
    Column(
       modifier =  Modifier
           .fillMaxWidth()
           .verticalScroll(verticalScrollState)
           .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.plant_popup_header),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val context = LocalContext.current
        DropDown<Months>(
            defaultName = uiState.month.localizedName(context),
            labelMapper = { it.localizedName(context) },
            onClick = {
                graphScreenViewModel.setMonth(it)
                graphScreenViewModel.updatePredictedClimate()},
        )
        Slider(
            value = uiState.yearSelected,
            valueRange = 2026f..2126f, // Hardcoded range for now
            steps = 100,
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.background,
                activeTickColor = MaterialTheme.colorScheme.secondary
            ),
            onValueChange = {
                graphScreenViewModel.updateSelectedYear(it)
                graphScreenViewModel.updatePredictedClimate()
            } ,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            )
        Text(
            stringResource(R.string.plant_selected_year, uiState.yearSelected.toInt()),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Column(
            verticalArrangement = spacedBy(1.dp)
        ) {
            Text(
                stringResource(R.string.plant_predicted_temp, uiState.predictedTemperature),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                stringResource(R.string.plant_predicted_humidity, uiState.predictedHumidity),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                stringResource(R.string.plant_predicted_sunshine, uiState.predictedSunshine),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                if (uiState.predictedPrecipitation != null){
                    stringResource(
                        R.string.plant_predicted_precipitation,
                        uiState.predictedPrecipitation * 30

                    )
                }else{
                    stringResource(R.string.too_few_precipitation_data)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
        ){

            // shows 3 best plant card
            repeat(3) {
                if (plantPq.isNotEmpty()) {
                    PlantCard(plantPq.poll()!!)
                }
            }
        }
    }
}
