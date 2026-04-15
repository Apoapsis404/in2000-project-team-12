package no.uio.ifi.in2000.team12.team12project.ui.graphs

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.data.frost.ObsStationsDataSource
import no.uio.ifi.in2000.team12.team12project.data.frost.WeatherChartData
import no.uio.ifi.in2000.team12.team12project.data.frost.WeatherDataRepository
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsType
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs

data class GraphScreenUiState(
    val weatherObs: List<WeatherObs> = emptyList(),
    val month: Months = Months.JANUARY,
    val obsType: ObsType = ObsType.MEAN_AIR_TEMP,
    val weatherChartData: WeatherChartData = WeatherChartData(emptyList(), emptyList()),
    val markerPosition : LatLng = LatLng(0.0,0.0),
    val isLoading : Boolean = false,
    val errorMessage : String? = null
)


class GraphScreenViewModel(
    application: Application
    //private val plantsRepository: PlantsRepository
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val weatherDataRepository = WeatherDataRepository()
    private val plantsRepository = PlantsRepository(context)
    private val obsDataSource = ObsStationsDataSource()

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var graphDataFetchJob : Job? = null


    init {
        getWeatherObs()
        //plantsRepository.getBestPlants(22).forEach {
        //    plantsRepository.printPlant(it)
        //}

    }

    fun getWeatherObs(minCount : Int = 0, maxCount : Int = 5) {
        graphDataFetchJob?.cancel()

        graphDataFetchJob = viewModelScope.launch {
            //println("position: " + _uiState.value.markerPosition)
            _uiState.update{it.copy(isLoading = true, errorMessage = null)}
            try {
                val nearestStation = obsDataSource.getNearestStation(
                    _uiState.value.markerPosition.latitude,
                    _uiState.value.markerPosition.longitude,
                    maxCount = maxCount
                )

                var weatherObs: List<WeatherObs> = emptyList()

                for (station in nearestStation.subList(minCount, maxCount)) {
                    try {
                        weatherObs = weatherDataRepository.getWeatherData(
                            station.id,
                            "mean(air_temperature P1M)",
                            "R10/2016-01-01/2017-01-01"
                        )

                    } catch (e: Exception) {
                        println(e.message)
                        continue
                    }
                    if (weatherObs.isNotEmpty()) {
                        //println(station.name)
                        break
                    }
                }
                if (weatherObs.isEmpty() && (maxCount < 25)) { //FIX
                    getWeatherObs(maxCount, maxCount + 5)
                    return@launch
                }
                if (weatherObs.isEmpty()){
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Fant ingen klimadata i dette området") }
                    return@launch
                }


                _uiState.update { it.copy(weatherObs = weatherObs, isLoading = false) }
                updateChartData()
            }catch(e : CancellationException) {
                throw e
            }catch (e : Exception){
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, errorMessage = "Noe gikk galt: ${e.message}") }
            }
        }
    }

    fun setMonth(month: Months) {
        _uiState.update { it.copy(month = month) }
        updateChartData()
    }
    fun setObsType(obsType: ObsType) {
        _uiState.update { it.copy(obsType = obsType) }
    }


    fun setMarkerPosition(position : LatLng){
        _uiState.update { it.copy(markerPosition = position) }
    }

    private fun updateChartData() {
        val weatherObs = weatherDataRepository.getObsFromMonth(_uiState.value.month)
        val chartData = weatherDataRepository.getYearAndData(weatherObs)
        _uiState.update { it.copy(weatherChartData = chartData) }
    }


}
