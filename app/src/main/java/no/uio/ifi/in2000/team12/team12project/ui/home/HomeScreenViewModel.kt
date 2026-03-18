package no.uio.ifi.in2000.team12.team12project.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.frost.ObsStationsDataSource
import no.uio.ifi.in2000.team12.team12project.data.frost.getStationIdsFromList
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import org.slf4j.Marker

data class HomeScreenUiState(
    val markerPosition : LatLng = LatLng(59.91,10.74)
)



class HomeScreenViewModel() : ViewModel(){
    private val obsStationsDataSource : ObsStationsDataSource = ObsStationsDataSource()

    private val _uiState = MutableStateFlow(HomeScreenUiState())

    val uiState = _uiState.asStateFlow()



    fun getNearestStationData(){
        viewModelScope.launch{
            val stationList : List<ObsStation> = obsStationsDataSource.getNearestStation(_uiState.value.markerPosition.latitude, _uiState.value.markerPosition.longitude)
            println(getStationIdsFromList(stationList))
        }
    }

    fun setMarkerPosition(position : LatLng){
        _uiState.update { it.copy(markerPosition = position) }
    }

    fun getMarkerPosition() : LatLng{
        return _uiState.value.markerPosition
    }
}


