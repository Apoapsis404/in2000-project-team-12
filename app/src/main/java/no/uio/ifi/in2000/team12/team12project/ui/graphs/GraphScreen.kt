package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.data.regression.LinearRegressionModel
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.DropDown
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.PlantPopup

// Graph library used:
// https://github.com/patrykandpatrick/vico

@OptIn(ExperimentalMaterial3Api::class) // For ModalBottomSheet (Plant popup)
@Composable
fun GraphScreen(
    modifier: Modifier = Modifier,
    graphScreenViewModel: GraphScreenViewModel = viewModel(),

) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val uiState by graphScreenViewModel.uiState.collectAsState()
    var yearSelected by remember { mutableStateOf(true) }
    val plantPopupState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()


    LaunchedEffect(uiState.climateChartData, uiState.showMean) {
        if (uiState.climateChartData.years.isNotEmpty() && uiState.climateChartData.values.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(uiState.climateChartData.years, uiState.climateChartData.values)
                    //println("${uiState.climateChartData.values} ${uiState.climateChartData.years}")
//                    val movingAverage = calculateWeightedMovingAverage(uiState.climateChartData.values, 3)
//                    if (movingAverage.isNotEmpty()) {
//                        series(uiState.climateChartData.years, movingAverage)
//                    }
                    series(uiState.climateChartData.years, LinearRegressionModel(uiState.climateChartData.years.toDoubleArray(), uiState.climateChartData.values.toDoubleArray()).fitToYears(uiState.climateChartData.years))
                    if (uiState.showMean) {
                        series(uiState.climateChartData.years, List(uiState.climateChartData.years.size) { uiState.climateChartData.values.average() })
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Column(
//            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when{
                uiState.isLoading -> {
                    Column(modifier = Modifier
                        .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(stringResource(R.string.loading_climate_data))
                    }
                }
                uiState.errorMessage != null ->{
                    Column(modifier = Modifier
                        .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.errorMessage}",
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Button(onClick =  { graphScreenViewModel.getClimateObs()} ){
                            Text(stringResource(R.string.try_again))
                        }
                    }
                }
                else -> {
                    key(uiState.climateType) {
                        ComposeClimateGraph(modelProducer, uiState.climateType.unit)
                    }
                }
            }

            Spacer(modifier = modifier.height(10.dp))

            Row(modifier = modifier.fillMaxWidth()) {
                DropDown<Months>(uiState.month.toString(), modifier = modifier.weight(1f)) {
                    graphScreenViewModel.setMonth(it)
                }
//                DropDown<ClimateType>(uiState.climateType.toString(), modifier = modifier.weight(1f)) {
//                    graphScreenViewModel.setClimateType(it)
//                    graphScreenViewModel.getClimateObs()
//                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Checkbox(
                    checked = uiState.showMean,
                    onCheckedChange = { graphScreenViewModel.toggleMean(it) }
                )
                Text(text = stringResource(R.string.show_avg))
            }
        }

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                GraphCard(
                    modifier = Modifier.weight(1f),
                    climateType = ClimateType.MEAN_AIR_TEMP,
                    meanValue = graphScreenViewModel.graphScreenData.getMean(ClimateType.MEAN_AIR_TEMP, uiState.month) ?: 0.0,
                    selected = uiState.climateType == ClimateType.MEAN_AIR_TEMP,
                    onClick = {
                        graphScreenViewModel.setClimateType(ClimateType.MEAN_AIR_TEMP)
                        graphScreenViewModel.updateChartData()
                    }
                )
                GraphCard(
                    modifier = Modifier.weight(1f),
                    climateType = ClimateType.RELATIVE_HUMIDITY,
                    meanValue = graphScreenViewModel.graphScreenData.getMean(ClimateType.RELATIVE_HUMIDITY, uiState.month) ?: 0.0,
                    selected = uiState.climateType == ClimateType.RELATIVE_HUMIDITY,
                    onClick = {
                        graphScreenViewModel.setClimateType(ClimateType.RELATIVE_HUMIDITY)
                        graphScreenViewModel.updateChartData()
                    }
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                GraphCard(
                    modifier = Modifier.weight(1f),
                    climateType = ClimateType.DURATION_OF_SUN,
                    meanValue = graphScreenViewModel.graphScreenData.getMean(ClimateType.DURATION_OF_SUN, uiState.month) ?: 0.0,
                    selected = uiState.climateType == ClimateType.DURATION_OF_SUN,
                    onClick = {
                        graphScreenViewModel.setClimateType(ClimateType.DURATION_OF_SUN)
                        graphScreenViewModel.updateChartData()
                    }

                )
                GraphCard(
                    modifier = Modifier.weight(1f),
                    climateType = ClimateType.PRECIPITATION,
                    meanValue = graphScreenViewModel.graphScreenData.getMean(ClimateType.PRECIPITATION, uiState.month) ?: 0.0,
                    selected = uiState.climateType == ClimateType.PRECIPITATION,
                    onClick = {
                        graphScreenViewModel.setClimateType(ClimateType.PRECIPITATION)
                        graphScreenViewModel.updateChartData()
                    }
                )
            }
            Button(
                onClick = {
                    graphScreenViewModel.openPopup()
                    graphScreenViewModel.updatePredictedClimate()
                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(stringResource(R.string.plant_prognosis_button))
            }
            if(uiState.showPopup) {
                ModalBottomSheet(
                    onDismissRequest = { graphScreenViewModel.closePopup() },
                    sheetState = plantPopupState,
                ){
                    PlantPopup(graphScreenViewModel, uiState)
                }
            }
        }
    }
}

private val RangeProvider =
    object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = minY

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) = maxY
    }

private fun getColumnProvider(positive: LineComponent, negative: LineComponent) =
    object : ColumnCartesianLayer.ColumnProvider {
        override fun getColumn(
            entry: ColumnCartesianLayerModel.Entry,
            seriesIndex: Int,
            extraStore: ExtraStore,
        ) = if (entry.y >= 0) positive else negative

        override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) = positive
    }

@Composable
private fun ComposeClimateGraph(
    modelProducer: CartesianChartModelProducer,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val startAxisValueFormatter = remember(unit) { CartesianValueFormatter.decimal(suffix = " $unit") }
    val axisLabelStyle = rememberTextComponent(
        TextStyle(color = MaterialTheme.colorScheme.onBackground)
    )
    val positiveColumn =
        rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.primary),
            thickness = 8.dp,
            shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40),
        )
    val negativeColumn =
        rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.tertiary),
            thickness = 8.dp,
            shape = RoundedCornerShape(bottomStartPercent = 40, bottomEndPercent = 40),
        )
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(
                    rangeProvider = RangeProvider,
                ),
//                rememberColumnCartesianLayer(
//                    columnProvider = remember(positiveColumn, negativeColumn) {
//                        getColumnProvider(positiveColumn, negativeColumn)
//                    },
//                    columnCollectionSpacing = 4.dp,
//                    rangeProvider = RangeProvider,
//                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = startAxisValueFormatter,
                    label = axisLabelStyle
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    labelRotationDegrees = 45f,
                    label = axisLabelStyle
                ),
            ),
        modelProducer = modelProducer,
        modifier = modifier.height(234.dp),
        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
    )
}
