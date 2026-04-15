package no.uio.ifi.in2000.team12.team12project.ui.graphs

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.R
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
    val weatherChartData: WeatherChartData = WeatherChartData(emptyList(), emptyList())
)


class GraphScreenViewModel(
    application: Application
    //private val plantsRepository: PlantsRepository
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val weatherDataRepository = WeatherDataRepository()
    private val plantsRepository = PlantsRepository(context)
    private val _uiState = MutableStateFlow(GraphScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getWeatherObs()
        plantsRepository.getBestPlants(22).forEach {
            plantsRepository.printPlant(it)
        }

    }

    fun getWeatherObs() {
        if(uiState.value.weatherObs.isNotEmpty()) return
        viewModelScope.launch {
            val weatherObs = weatherDataRepository.getWeatherData("SN18700", "mean(air_temperature P1M)", "R10/2010-01-01/2011-01-01")
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

    private fun updateChartData() {
        val weatherObs = weatherDataRepository.getObsFromMonth(_uiState.value.month)
        val chartData = weatherDataRepository.getYearAndData(weatherObs)
        _uiState.update { it.copy(weatherChartData = chartData) }
    }


}
