package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.data.regression.LinearRegressionModel
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.localizedName
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.DropDown
import no.uio.ifi.in2000.team12.team12project.ui.customComposables.PlantPopup

/**
 * Screen displaying climate data graphs and prognosis.
 *
 * Refactoring:
 * - Removed redundant key() wrappers.
 * - Used theme-aware colors (MaterialTheme.colorScheme.error).
 * - Improved state handling and null safety for error messages.
 * - Added documentation and clarified layout logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    modifier: Modifier = Modifier,
    graphScreenViewModel: GraphScreenViewModel,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val uiState by graphScreenViewModel.uiState.collectAsState()
    val plantPopupState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Sync climate data with the chart producer whenever it changes
    LaunchedEffect(uiState.climateChartData) {
        val data = uiState.climateChartData
        if (data.years.isNotEmpty() && data.values.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    // Original data points
                    series(data.years, data.values)

                    // Add a regression line to visualize trends
                    val regression = LinearRegressionModel(
                        data.years.toDoubleArray(),
                        data.values.toDoubleArray()
                    )
                    series(data.years, regression.fitToYears(data.years))
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Handle different UI states (Loading, Error, Content)
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(
                    error = uiState.errorMessage ?: "",
                    onRetry = { graphScreenViewModel.fetchAllClimateData() }
                )
                else -> {
                    // Display the main graph. The graph will automatically refresh when units change.
                    ComposeClimateGraph(
                        modelProducer = modelProducer,
                        unit = uiState.climateType.unit
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month selection dropdown
            DropDown<Months>(
                defaultName = uiState.month.localizedName(context),
                labelMapper = { it.localizedName(context) }
            ) { selectedMonth ->
                graphScreenViewModel.setMonth(selectedMonth)
            }
        }

        // Grid of interactive climate property cards
        ClimateCardGrid(
            uiState = uiState,
            onCardClick = { type -> graphScreenViewModel.setClimateType(type) },
            getMean = { type, month -> graphScreenViewModel.getMean(type, month) }
        )

        // Button to open the plant prognosis popup
        Button(
            onClick = { graphScreenViewModel.openPopup() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(stringResource(R.string.plant_prognosis_button))
        }

        // Plant prognosis bottom sheet
        if (uiState.showPopup) {
            ModalBottomSheet(
                onDismissRequest = { graphScreenViewModel.closePopup() },
                containerColor = MaterialTheme.colorScheme.background,
                sheetState = plantPopupState,
                dragHandle = {
                    BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary)
                }
            ) {
                PlantPopup(graphScreenViewModel, uiState)
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.loading_climate_data),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.try_again))
        }
    }
}

@Composable
private fun ClimateCardGrid(
    uiState: GraphScreenUiState,
    onCardClick: (ClimateType) -> Unit,
    getMean: (ClimateType, Months) -> Double?
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val climateTypes = listOf(
        ClimateType.MEAN_AIR_TEMP,
        ClimateType.RELATIVE_HUMIDITY,
        ClimateType.DURATION_OF_SUN,
        ClimateType.PRECIPITATION
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp),
        maxItemsInEachRow = if (isLandscape) 4 else 2
    ) {
        climateTypes.forEach { type ->
            val isAvailable = uiState.availableClimateTypes.contains(type)
            GraphCard(
                modifier = Modifier.weight(1f),
                climateType = type,
                meanValue = getMean(type, uiState.month) ?: 0.0,
                selected = uiState.climateType == type,
                onClick = { onCardClick(type) },
                enabled = isAvailable
            )
        }
    }
}

// Vico RangeProvider to ensure axis matches data bounds exactly
private val RangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = minY
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) = maxY
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

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(rangeProvider = RangeProvider),
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
        modifier = modifier.height(240.dp),
        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
    )
}
