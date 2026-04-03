package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.frost.ObsStationsDataSource
import no.uio.ifi.in2000.team12.team12project.data.frost.WeatherChartData
import no.uio.ifi.in2000.team12.team12project.data.frost.WeatherDataRepository
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsType
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs

data class GraphScreenUiState(
    val weatherObs: List<WeatherObs> = emptyList(),
    val month: Months = Months.JANUARY,
    val obsType: ObsType = ObsType.MEAN_AIR_TEMP,
    val weatherChartData: WeatherChartData = WeatherChartData(emptyList(), emptyList()),
    val markerPosition : LatLng = LatLng(0.0,0.0)
)


class GraphScreenViewModel : ViewModel() {

    private val weatherDataRepository = WeatherDataRepository()
    private val obsDataSource = ObsStationsDataSource()

    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getWeatherObs()
    }



    fun getWeatherObs() {
        _uiState.update { it.copy(weatherObs = emptyList()) }
        //if(uiState.value.weatherObs.isNotEmpty()) return
        viewModelScope.launch {
            println(_uiState.value.markerPosition)
            val nearestStation = obsDataSource.getNearestStation(_uiState.value.markerPosition.latitude,_uiState.value.markerPosition.longitude)
            var weatherObs : List<WeatherObs> = emptyList()

            for (station in nearestStation){
                try{
                    weatherObs = weatherDataRepository.getWeatherData(station.id, "mean(air_temperature P1M)", "R10/2016-01-01/2017-01-01")

                }catch (e : Exception){
                    println(e.message)
                    continue
                }
                if (weatherObs.isNotEmpty()){
                    println(station.name)
                    break
                }
            }


            _uiState.update { it.copy(weatherObs = weatherObs) }
            updateChartData()
        }
    }

    fun setMonth(month: Months) {
        _uiState.update { it.copy(month = month) }
        updateChartData()
    }
    fun setObsType(obsType: ObsType) {
        _uiState.update { it.copy(obsType = obsType) }
    }

    fun getWeatherChartData() {
        updateChartData()
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