package no.uio.ifi.in2000.team12.team12project.ui.graphs

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.data.frost.constructTimeSeries
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs

data class GraphScreenUiState(
    val climateObs: List<ClimateObs> = emptyList(),
    val month: Months = Months.JANUARY,
    val climateType: ClimateType = ClimateType.MEAN_AIR_TEMP,
    val climateChartData: ClimateChartData = ClimateChartData(emptyList(), emptyList()),
    val markerPosition : LatLng = LatLng(0.0,0.0),
    val isLoading : Boolean = false,
    val errorMessage : String? = null,
    val showMean: Boolean = false,
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

    private var graphDataFetchJob : Job? = null

    fun getClimateObs() {
        Log.d("GraphScreenViewModel", "Climate type: ${_uiState.value.climateType}")
        graphDataFetchJob?.cancel()

        graphDataFetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val obs = climateDataRepository.getClimateDataFailing(
                    _uiState.value.markerPosition,
                    _uiState.value.climateType,
                    constructTimeSeries(120u)
                )
                _uiState.update { it.copy(climateObs = obs, isLoading = false) }
                Log.d("GraphScreenViewModel", "Time series: ${constructTimeSeries(120u)}")
                updateChartData()
            } catch (e: NoAvailableTimeSeriesException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = context.getString(R.string.no_data_available)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "An unexpected error occurred") }
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

    private fun updateChartData(climateObsList: List<ClimateObs> = _uiState.value.climateObs) {
        val climateObs = climateDataRepository.getObsFromMonth(_uiState.value.month, climateObsList)
        val chartData = climateDataRepository.getYearAndData(climateObs)
        _uiState.update { it.copy(climateChartData = chartData) }
    }
}
