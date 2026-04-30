package no.uio.ifi.in2000.team12.team12project.ui.CustomComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenUiState
import no.uio.ifi.in2000.team12.team12project.ui.graphs.GraphScreenViewModel

@Composable
fun PlantPopup(
    graphScreenViewModel: GraphScreenViewModel,
    uiState: GraphScreenUiState,
    modifier: Modifier = Modifier
){
    Column(
       modifier =  Modifier
           .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.plant_popup_header))
        DropDown<Months>(uiState.month.toString(), modifier = modifier.weight(1f)) {
            graphScreenViewModel.setMonth(it)
        }
    }
}
