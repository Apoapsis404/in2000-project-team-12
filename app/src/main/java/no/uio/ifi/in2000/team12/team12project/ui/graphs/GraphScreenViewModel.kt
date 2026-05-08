package no.uio.ifi.in2000.team12.team12project.ui.graphs

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository
import no.uio.ifi.in2000.team12.team12project.data.regression.LinearRegressionModel
import no.uio.ifi.in2000.team12.team12project.model.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.plants.Plant
import java.util.PriorityQueue

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
    val yearSelected: Float = 2026f,
    val predictedTemperature: Float = 15f,
    val predictedHumidity: Float = 100f,
    val predictedSunshine: Float = 100f,
    val predictedPrecipitation: Float = 100f,
)


class GraphScreenViewModel(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val climateDataRepository = ClimateDataRepository()
    private val plantsRepository = PlantsRepository(context)

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState = _uiState.asStateFlow()

    // Cache maps, previously in GraphScreenData
    private val climateMap = mutableMapOf<ClimateType, ClimateData>()
    private val meansMap = mutableMapOf<Pair<ClimateType, Months>, Double>()
    private val chartDataMap = mutableMapOf<Pair<ClimateType, Months>, ClimateChartData>()
    private var tempRegressionModel : LinearRegressionModel? = null// = LinearRegressionModel(uiState.value.climateChartData.years.toDoubleArray(), uiState.value.climateChartData.values.toDoubleArray())
    private var precRegressionModel : LinearRegressionModel? = null


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
        if (chartDataMap.containsKey(key)) return chartDataMap[key]
        val data = climateMap[climateType] ?: return null
        val obsFromMonth = getObsFromMonth(month, data.climateObs)
        val chartData = getYearAndData(obsFromMonth)
        chartDataMap[key] = chartData
        return chartData
    }

    // --- Public API ---

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
                val refTime = constructTimeSeries(120u)

                val deferreds = ClimateType.entries.map { type ->
                    async {
                        try {
                            val obs = climateDataRepository.getClimateDataFailing(position, type, refTime)
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

//    fun toggleMean(show: Boolean) {
//        _uiState.update { it.copy(showMean = show) }
//    }

    fun getMean(climateType: ClimateType, month: Months): Double? {
        return meansMap[Pair(climateType, month)]
    }

    fun updateChartData(climateType: ClimateType = _uiState.value.climateType) {
        val chartData = getClimateChartData(climateType, _uiState.value.month)
        _uiState.update {
            it.copy(climateChartData = chartData ?: ClimateChartData(emptyList(), emptyList()))
        }

        // To update regression model when chart is updated
        try {
            val tempData = getClimateChartData(ClimateType.MEAN_AIR_TEMP, month = uiState.value.month)!!
            tempRegressionModel = LinearRegressionModel(tempData.years.toDoubleArray(), tempData.values.toDoubleArray())

            val precData = getClimateChartData(ClimateType.PRECIPITATION, month = uiState.value.month)!!
            precRegressionModel = LinearRegressionModel(precData.years.toDoubleArray(), precData.values.toDoubleArray())

        }catch (
            e: Exception
        ){
            Log.e("GraphViewModel", "Failed to update regression model", e)
        }
    }

    fun updatePredictedClimate() {
        _uiState.update {
            it.copy(
                predictedTemperature = tempRegressionModel?.predict(it.yearSelected.toInt())?.toFloat() ?: 0f,
                predictedHumidity = getMean(ClimateType.RELATIVE_HUMIDITY, uiState.value.month)?.toFloat() ?: 0f,
                predictedSunshine = getMean(ClimateType.DURATION_OF_SUN, uiState.value.month)?.toFloat() ?: 0f,
                predictedPrecipitation = precRegressionModel?.predict(it.yearSelected.toInt())?.toFloat() ?: 0f,
            )
        }
    }

    fun openPopup() {
        _uiState.update { it.copy(showPopup = true) }
    }

    fun closePopup() {
        _uiState.update { it.copy(showPopup = false) }
    }

    fun updateSelectedYear(year: Float) {
        _uiState.update { it.copy(yearSelected = year) }
    }

    fun getBestPlants(
        targetTemp: Float,
        targetHumidity: Float = 50f,
        targetSunshine: Float = 7f,
        targetPrecipitation: Float = 5f
    ): PriorityQueue<Plant> {
        return plantsRepository.getBestPlantsPriorityQueue(
            targetTemp, targetHumidity, targetSunshine, targetPrecipitation
        )
    }
}