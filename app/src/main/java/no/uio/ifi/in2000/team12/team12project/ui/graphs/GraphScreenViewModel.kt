package no.uio.ifi.in2000.team12.team12project.ui.graphs

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
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.frost.Round
import no.uio.ifi.in2000.team12.team12project.data.frost.constructTimeSeries
import no.uio.ifi.in2000.team12.team12project.data.frost.findMeanValue
import no.uio.ifi.in2000.team12.team12project.data.frost.getObsFromMonth
import no.uio.ifi.in2000.team12.team12project.data.frost.getYearAndData
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository
import no.uio.ifi.in2000.team12.team12project.model.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.model.Plants.Plant
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateData
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import java.util.PriorityQueue

data class GraphScreenUiState(
    val climateObs: List<ClimateObs> = emptyList(),
    val month: Months = Months.JANUARY,
    val climateType: ClimateType = ClimateType.MEAN_AIR_TEMP,
    val climateChartData: ClimateChartData = ClimateChartData(emptyList(), emptyList()),
    val markerPosition : LatLng = LatLng(0.0,0.0),
    val isLoading : Boolean = false,
    val errorMessage : String? = null,
    val showMean: Boolean = false,
    val showPopup: Boolean = true, // sett til false
    val yearSelected: Float = 2026f,
    val predictedTemperature: Float = 0f,
)


class GraphScreenViewModel(
    application: Application
    //private val plantsRepository: PlantsRepository
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val climateDataRepository = ClimateDataRepository()
    private val plantsRepository = PlantsRepository(context)

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState = _uiState.asStateFlow()

    val graphScreenData = GraphScreenData(_uiState.value.markerPosition)

    private var graphDataFetchJob : Job? = null

    fun getClimateObs(){
        _uiState.update{
            it.copy(climateObs = graphScreenData.getClimateData(_uiState.value.climateType)!!.climateObs)
        }
    }
    fun fetchAllClimateData() {
        val position = _uiState.value.markerPosition
        graphDataFetchJob?.cancel()

        graphDataFetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val refTime = constructTimeSeries(120u)

                // Fetches all climate types in parallel
                val deferreds = ClimateType.entries.map { type ->
                    async {
                        try {
                            val obs = climateDataRepository.getClimateDataFailing(position, type, refTime)
                            val climateData = ClimateData(refTime, obs, type)
                            graphScreenData.setClimateData(type, climateData)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.e("GraphViewModel", "Failed to fetch $type", e)
                        }
                    }
                }
                deferreds.awaitAll()

                updateChartData()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error loading data") }
            }
        }
    }


    fun setMonth(month: Months) {
        _uiState.update { it.copy(month = month) }
        updateChartData()
    }

    fun setClimateType(climateType: ClimateType) {
        _uiState.update { it.copy(climateType = climateType) }
    }

    fun setMarkerPosition(position : LatLng){
        _uiState.update { it.copy(markerPosition = position) }
    }

    fun toggleMean(show: Boolean) {
        _uiState.update { it.copy(showMean = show) }
    }

    fun getMean(climateType: ClimateType, month: Months): Double? {
        if (!graphScreenData.containsKey(climateType, month)) {
            // Calculate mean and store it in the map
            val obsFromMonth = getObsFromMonth(month, graphScreenData.getClimateData(climateType)!!.climateObs)
            val mean = findMeanValue(obsFromMonth)
            graphScreenData.setMean(climateType, month, mean)
            return mean
        }
        return graphScreenData.getMean(climateType, month)
    }

    private fun updateChartData(climateType: ClimateType = _uiState.value.climateType) {
        val chartData: ClimateChartData? = graphScreenData.getClimateChartData(climateType, _uiState.value.month)
        if (chartData == null) {
            _uiState.update { it.copy(climateChartData = ClimateChartData(emptyList(), emptyList())) }
            return
        }
        _uiState.update { it.copy(climateChartData = chartData) }
    }

    // REPLACE THIS FUNCTION
    fun updatePredictedTemperature(){
        _uiState.update { it.copy(predictedTemperature = (it.yearSelected - 2026) * 0.2f +10)}
    }

    fun openPopup() {
        _uiState.update { it.copy(showPopup = true) }
    }
    fun closePopup(){
        _uiState.update { it.copy(showPopup = false) }
    }
    fun updateSelectedYear(year: Float) {
        _uiState.update { it.copy(yearSelected = year) }
    }
    fun getBestPlants(targetTemp: Float): PriorityQueue<Plant>{
        return plantsRepository.getBestPlantsPriorityQueue(targetTemp)
    }


}
