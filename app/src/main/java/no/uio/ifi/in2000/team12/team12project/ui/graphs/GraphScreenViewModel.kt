package no.uio.ifi.in2000.team12.team12project.ui.graphs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.frost.constructTimeSeries
import no.uio.ifi.in2000.team12.team12project.data.frost.findMeanValue
import no.uio.ifi.in2000.team12.team12project.data.frost.getObsFromMonth
import no.uio.ifi.in2000.team12.team12project.data.frost.getYearAndData
import no.uio.ifi.in2000.team12.team12project.data.frost.monthsSince
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository
import no.uio.ifi.in2000.team12.team12project.data.regression.LinearRegressionModel
import no.uio.ifi.in2000.team12.team12project.model.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.plants.Plant
import java.util.PriorityQueue
import kotlin.math.min

data class GraphScreenUiState(
    val climateObs: List<ClimateObs> = emptyList(),
    val month: Months = Months.JANUARY,
    val climateType: ClimateType = ClimateType.MEAN_AIR_TEMP,
    val climateChartData: ClimateChartData = ClimateChartData(emptyList(), emptyList()),
    val markerPosition: LatLng = LatLng(0.0, 0.0),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showMean: Boolean = false,
    val showPopup: Boolean = false,
    val yearSelected: Float = GraphScreenViewModel.DEFAULT_YEAR_SELECTED,
    val predictedTemperature: Float = GraphScreenViewModel.DEFAULT_PREDICTED_TEMP,
    val predictedHumidity: Float = GraphScreenViewModel.DEFAULT_PREDICTED_HUMIDITY,
    val predictedSunshine: Float = GraphScreenViewModel.DEFAULT_PREDICTED_SUNSHINE,
    val predictedPrecipitationDaily: Float? = GraphScreenViewModel.DEFAULT_PREDICTED_PRECIPITATION,
    val predictedPrecipitationMonthly: Float? = null,
    val recommendedPlants: List<Plant> = emptyList(),
)

class GraphScreenViewModel(
    private val climateDataRepository: ClimateDataRepository,
    private val plantsRepository: PlantsRepository
) : ViewModel() {

    companion object {
        const val MAX_MONTHS_OF_HISTORY = 360u
        const val MIN_DATA_POINTS_FOR_REGRESSION = 10
        
        const val DEFAULT_YEAR_SELECTED = 2026f
        const val DEFAULT_PREDICTED_TEMP = 15f
        const val DEFAULT_PREDICTED_HUMIDITY = 100f
        const val DEFAULT_PREDICTED_SUNSHINE = 100f
        const val DEFAULT_PREDICTED_PRECIPITATION = 100f

        const val FALLBACK_HUMIDITY = 50f
        const val FALLBACK_SUNSHINE = 7f
        const val FALLBACK_PRECIPITATION = 5f

        // New Constants for logic and UI sync
        const val DAYS_IN_MONTH = 30f
        const val MIN_YEAR = 2026f
        const val MAX_YEAR = 2126f
        const val RECOMMENDED_PLANTS_LIMIT = 3
    }

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val climateMap = mutableMapOf<ClimateType, ClimateData>()
    private val meansMap = mutableMapOf<Pair<ClimateType, Months>, Double>()
    private val chartDataMap = mutableMapOf<Pair<ClimateType, Months>, ClimateChartData>()
    
    private var tempRegressionModel: LinearRegressionModel? = null
    private var precRegressionModel: LinearRegressionModel? = null

    private var graphDataFetchJob: Job? = null

    private fun clearCachedData() {
        climateMap.clear()
        meansMap.clear()
        chartDataMap.clear()
    }

    private fun setClimateData(climateType: ClimateType, climateData: ClimateData) {
        climateMap[climateType] = climateData
    }

    private fun getClimateData(climateType: ClimateType): ClimateData? {
        return climateMap[climateType]
    }

    private fun computeAllMeans() {
        for (climateType in ClimateType.entries) {
            for (month in Months.entries) {
                val obs = getObsFromMonth(month, climateMap[climateType]?.climateObs ?: continue)
                meansMap[Pair(climateType, month)] = findMeanValue(obs)
            }
        }
    }

    private fun getClimateChartData(climateType: ClimateType, month: Months): ClimateChartData? {
        val key = Pair(climateType, month)
        chartDataMap[key]?.let { return it }
        
        val data = climateMap[climateType] ?: return null
        val obsFromMonth = getObsFromMonth(month, data.climateObs)
        val chartData = getYearAndData(obsFromMonth)
        chartDataMap[key] = chartData
        return chartData
    }

    fun setMarkerPositionAndFetch(position: LatLng) {
        clearCachedData()
        _uiState.update { it.copy(markerPosition = position) }
        fetchAllClimateData()
    }

    fun fetchAllClimateData() {
        val position = _uiState.value.markerPosition
        graphDataFetchJob?.cancel()

        graphDataFetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val deferreds = ClimateType.entries.map { type ->
                    async {
                        try {
                            val (timeSeries, source) = climateDataRepository.getClimateTimeSeriesFromPos(position, type)
                            val months = monthsSince(timeSeries)
                            val refTime = constructTimeSeries(min(months.toUInt(), MAX_MONTHS_OF_HISTORY))
                            val obs = climateDataRepository.getClimateData(source, type.elementId, refTime)
                            val climateData = ClimateData(refTime, obs, type)
                            setClimateData(type, climateData)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e("GraphViewModel", "Failed to fetch $type", e)
                        }
                    }
                }
                deferreds.awaitAll()
                computeAllMeans()
                updateChartData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error loading data ${e.message}")
                }
            }
        }
    }

    fun getClimateObs() {
        _uiState.update {
            it.copy(climateObs = getClimateData(it.climateType)?.climateObs ?: emptyList())
        }
    }

    fun setMonth(month: Months) {
        _uiState.update { it.copy(month = month) }
        updateChartData()
    }

    fun setClimateType(climateType: ClimateType) {
        _uiState.update { it.copy(climateType = climateType) }
        updateChartData()
    }

    fun getMean(climateType: ClimateType, month: Months): Double? {
        return meansMap[Pair(climateType, month)]
    }

    fun updateChartData(climateType: ClimateType = _uiState.value.climateType) {
        val currentMonth = _uiState.value.month
        val chartData = getClimateChartData(climateType, currentMonth)
        
        _uiState.update {
            it.copy(climateChartData = chartData ?: ClimateChartData(emptyList(), emptyList()))
        }

        try {
            getClimateChartData(ClimateType.MEAN_AIR_TEMP, currentMonth)?.let { tempData ->
                tempRegressionModel = LinearRegressionModel(tempData.years.toDoubleArray(), tempData.values.toDoubleArray())
            }

            getClimateChartData(ClimateType.PRECIPITATION, currentMonth)?.let { precData ->
                precRegressionModel = if (precData.years.size < MIN_DATA_POINTS_FOR_REGRESSION) {
                    null
                } else {
                    LinearRegressionModel(precData.years.toDoubleArray(), precData.values.toDoubleArray())
                }
            }
            
            // Automatically update predictions when data models are updated
            updatePredictedClimateInternal()
            
        } catch (e: Exception) {
            Log.e("GraphViewModel", "Failed to update regression model", e)
        }
    }

    /**
     * Logic to update all predicted values based on current month and selected year.
     * This is private as it is triggered internally by state changes (Month/Year/Data).
     */
    private fun updatePredictedClimateInternal() {
        val currentMonth = _uiState.value.month
        val selectedYear = _uiState.value.yearSelected.toInt()
        
        val temp = tempRegressionModel?.predict(selectedYear)?.toFloat() ?: 0f
        val hum = getMean(ClimateType.RELATIVE_HUMIDITY, currentMonth)?.toFloat() ?: 0f
        val sun = getMean(ClimateType.DURATION_OF_SUN, currentMonth)?.toFloat() ?: 0f
        val precDaily = precRegressionModel?.predict(selectedYear)?.toFloat()
        
        // Convert daily average to monthly total for display
        val precMonthly = precDaily?.let { it * DAYS_IN_MONTH }

        // Fetch recommendations based on these predictions
        val plantsPq = plantsRepository.getBestPlantsPriorityQueue(
            temp, hum, sun, precDaily
        )
        val bestPlants = mutableListOf<Plant>()
        repeat(RECOMMENDED_PLANTS_LIMIT) {
            if (plantsPq.isNotEmpty()) bestPlants.add(plantsPq.poll()!!)
        }

        _uiState.update {
            it.copy(
                predictedTemperature = temp,
                predictedHumidity = hum,
                predictedSunshine = sun,
                predictedPrecipitationDaily = precDaily,
                predictedPrecipitationMonthly = precMonthly,
                recommendedPlants = bestPlants
            )
        }
    }

    fun openPopup() {
        _uiState.update { it.copy(showPopup = true) }
        updatePredictedClimateInternal()
    }

    fun closePopup() {
        _uiState.update { it.copy(showPopup = false) }
    }

    fun updateSelectedYear(year: Float) {
        _uiState.update { it.copy(yearSelected = year) }
        updatePredictedClimateInternal()
    }

    // This method is now redundant for the Popup but kept for other potential callers
    fun getBestPlants(
        targetTemp: Float,
        targetHumidity: Float = FALLBACK_HUMIDITY,
        targetSunshine: Float = FALLBACK_SUNSHINE,
        targetPrecipitation: Float? = FALLBACK_PRECIPITATION
    ): PriorityQueue<Plant> {
        return plantsRepository.getBestPlantsPriorityQueue(
            targetTemp, targetHumidity, targetSunshine, targetPrecipitation
        )
    }
}
