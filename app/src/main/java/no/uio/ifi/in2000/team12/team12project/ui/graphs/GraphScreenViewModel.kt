package no.uio.ifi.in2000.team12.team12project.ui.graphs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.frost.WeatherDataRepository

class GraphScreenViewModel : ViewModel() {

    private val weatherDataRepository = WeatherDataRepository()


    fun getWeatherObs() {
        viewModelScope.launch {

        }
    }
}