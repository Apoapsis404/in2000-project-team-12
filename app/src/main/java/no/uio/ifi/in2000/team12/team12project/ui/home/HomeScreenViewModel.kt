package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.data.frost.ObsStationsDataSource
import no.uio.ifi.in2000.team12.team12project.data.frost.TorrentialRain
import no.uio.ifi.in2000.team12.team12project.data.frost.getStationIdsFromList
import no.uio.ifi.in2000.team12.team12project.data.nve.NVEDataSource
import no.uio.ifi.in2000.team12.team12project.data.nve.NVERepository
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation

// Represent the different states exposed to the UI
data class HomeScreenUiState(
    val markerPosition : LatLng = LatLng(59.91,10.74),
    val hasInitialLocationBeenSet: Boolean = false,
    val address: String = "Almanakken",
    val searchQuery : String = "",
    val markerTitle : String = "",
    val isMapReady : Boolean = false,
    val suggestions: List<String> = emptyList(),
    val downpourList: List<LatLng> = emptyList(),
    val extremeWindList: List<LatLng> = emptyList(),
    val flomList: List<LatLng> = emptyList()
)

class HomeScreenViewModel(application : Application) : AndroidViewModel(application){
    private val torrentialRain = TorrentialRain()
    private val obsStationsDataSource : ObsStationsDataSource = ObsStationsDataSource()
    private val nveRepository : NVERepository = NVERepository(NVEDataSource(application))
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)


    private val placesClient = Places.createClient(application)
    private val sessionToken = AutocompleteSessionToken.newInstance()

    init {
        readLatLngIntoList()
        fetchFloodWarnings()
    }

    fun fetchSuggestions(query: String) {
        println("fetchSuggestions called with: $query")
        if (query.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(sessionToken)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val suggestions = response.autocompletePredictions
                    .map { it.getFullText(null).toString() }
                println("Suggestions received: $suggestions")
                _uiState.update { it.copy(suggestions = suggestions) }
            }
            .addOnFailureListener { exception ->
                println("Places autocomplete failed: ${exception.message}")
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(suggestions = emptyList()) }
    }

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
                            markerTitle = "Your position"
                        )
                    }
                    onLocationFound(userPosition)
                }
            }
    }



    private fun fetchFloodWarnings() {
        viewModelScope.launch {
            try {
                val warnings = nveRepository.getFloodWarnings()
                //println("Fetched ${warnings.size} flood warnings")
                val latLngs = warnings.flatMap { warning ->
                    warning.municipalityList.map {
                        //println("flood warning at ${it.lat}, ${it.lon}")
                        LatLng(it.lat!!, it.lon!!)
                    }
                }
                //println("Converted to ${latLngs.size} LatLng markers")
                //val latLngs = warnings.map { LatLng(it.first, it.second) }
                _uiState.update { it.copy(flomList = latLngs) }
            } catch (e: Exception) {
                println("Error fetching flood warnings: ${e.message}")
                e.printStackTrace()
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

    fun updateAddress(address : String){
        _uiState.update { it.copy(address = address) }

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
    fun onMapReady(){
        _uiState.update { it.copy(isMapReady = true) }
    }


    fun vectorToBitMap(
        context : Context,
        vectorResId : Int,
        targetWidth : Int = 150,
        targetHeight : Int = 150
    ) : BitmapDescriptor?
    {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, targetWidth, targetHeight)

        val bitMap = createBitmap(targetWidth, targetHeight)
        val canvas = android.graphics.Canvas(bitMap)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitMap)
    }

    fun readLatLngIntoList() {
        val context = getApplication<Application>().applicationContext
        val torrentialRainStations = torrentialRain.readLatLngFromJson(context, "torrential_rain_stations.json")
        val highWindStations = torrentialRain.readLatLngFromJson(context, "high_wind_speed_stations.json")

        _uiState.update {
            it.copy(
                downpourList = torrentialRainStations,
                extremeWindList = highWindStations
            )
        }
    }

    fun setInitialLocationFlag(){
        _uiState.update { it.copy(hasInitialLocationBeenSet = true) }
    }
}


