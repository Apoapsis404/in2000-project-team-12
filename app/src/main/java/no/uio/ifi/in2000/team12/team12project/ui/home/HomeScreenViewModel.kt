package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.frost.ObsStationsDataSource
import no.uio.ifi.in2000.team12.team12project.data.frost.getStationIdsFromList
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation

// Represent the different states exposed to the UI
data class HomeScreenUiState(
    val markerPosition : LatLng = LatLng(59.91,10.74),
    val searchQuery : String = "",
    val markerTitle : String = ""
)



class HomeScreenViewModel(application : Application) : AndroidViewModel(application){
    private val obsStationsDataSource : ObsStationsDataSource = ObsStationsDataSource()
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchCurrentLocation(onLocationFound: (LatLng) -> Unit) {
        val locationRequest = com.google.android.gms.location.CurrentLocationRequest.Builder()
            .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location ->
                location?.let {
                    val userPosition = LatLng(it.latitude, it.longitude)
                    setMarkerPosition(userPosition)
                    _uiState.update { state ->
                        state.copy(
                            searchQuery = "",
                            markerTitle = "Din posisjon"
                        )
                    }
                    onLocationFound(userPosition)
                }
            }
    }

    fun getNearestStations(){    // Gets the nearest weather stations based on marker position values in _uiState and prints them to Log
        viewModelScope.launch{
            val lat = _uiState.value.markerPosition.latitude
            val lon = _uiState.value.markerPosition.longitude
            val stationList : List<ObsStation> = obsStationsDataSource.getNearestStation(lat, lon)
            println(getStationIdsFromList(stationList))  // prints list to log - only for bug testing
        }
    }
    // Takes in a position and updates marker position
    fun setMarkerPosition(position : LatLng){
        _uiState.update { it.copy(markerPosition = position) }
    }

    // reads the content of the search bar and updates query
    fun setSearchQuery(query : String){
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getMarkerPosition() : LatLng{
        return _uiState.value.markerPosition
    }

    // updates values when a location is searched in the search bar
    fun onSearchedLocation(query : String, position : LatLng){
        _uiState.update {
            it.copy(
                searchQuery = query,
                markerTitle = query,
                markerPosition = position
            )
        }
    }
    // updates values whenever a location is pressed on the map
    fun onMapClick(latLng: LatLng) {
        _uiState.update {
            it.copy(
                markerPosition = latLng,
                searchQuery = "",
                markerTitle = ""
            )
        }
    }
}


