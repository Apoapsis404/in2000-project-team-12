package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsType
import no.uio.ifi.in2000.team12.team12project.ui.CustomComposables.DropDown
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

// Graph library used:
// https://github.com/patrykandpatrick/vico


@Composable
fun GraphScreen(
    modifier: Modifier = Modifier,
    graphScreenViewModel: GraphScreenViewModel = viewModel(),
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val uiState by graphScreenViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.weatherChartData) {
        if (uiState.weatherChartData.years.isNotEmpty() && uiState.weatherChartData.values.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(uiState.weatherChartData.years, uiState.weatherChartData.values) }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {
        ComposeTemperatureAnomalies(modelProducer)
        Spacer(modifier = modifier.height(10.dp))
        DropDown<Months>(uiState.month.name, modifier = modifier.fillMaxWidth()) {
            graphScreenViewModel.setMonth(it)
        }
        DropDown<ObsType>(uiState.obsType.name, modifier = modifier.fillMaxWidth()) {
            graphScreenViewModel.setObsType(it)
        }
    }
}

private const val RANGE_PROVIDER_BASE = 0.1

private val RangeProvider =
    object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
            -RANGE_PROVIDER_BASE * ceil(max(abs(minY), maxY) / RANGE_PROVIDER_BASE)

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
            -getMinY(minY, maxY, extraStore)
    }

private val StartAxisValueFormatter = CartesianValueFormatter.decimal(suffix = " °C")

private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(suffix = " °C")

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
private fun ComposeTemperatureAnomalies(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    val positiveColumn =
        rememberLineComponent(
            fill = Fill(Color(0xff0ac285)),
            thickness = 8.dp,
            shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40),
        )
    val negativeColumn =
        rememberLineComponent(
            fill = Fill(Color(0xffe8304f)),
            thickness = 8.dp,
            shape = RoundedCornerShape(bottomStartPercent = 40, bottomEndPercent = 40),
        )
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider =
                        remember(positiveColumn, negativeColumn) {
                            getColumnProvider(positiveColumn, negativeColumn)
                        },
                    columnCollectionSpacing = 4.dp,
                    rangeProvider = RangeProvider,
                ),
                startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
                bottomAxis = HorizontalAxis.rememberBottom(labelRotationDegrees = 45f),
            ),
        modelProducer = modelProducer,
        modifier = modifier.height(234.dp),
        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
    )
}
