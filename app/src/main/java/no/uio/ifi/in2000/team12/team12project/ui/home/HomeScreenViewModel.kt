package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import no.uio.ifi.in2000.team12.team12project.data.frost.TorrentialRain
import no.uio.ifi.in2000.team12.team12project.data.nve.NVEDataSource
import no.uio.ifi.in2000.team12.team12project.data.nve.NVERepository

data class HomeScreenUiState(
    val markerPosition: LatLng = LatLng(59.91, 10.74),
    val hasInitialLocationBeenSet: Boolean = false,
    val address: String = "",
    val searchQuery: String = "",
    val markerTitle: String = "",
    val isMapReady: Boolean = false,
    val suggestions: List<String> = emptyList(),
    val downpourList: List<LatLng> = emptyList(),
    val extremeWindList: List<LatLng> = emptyList(),
    val flomList: List<LatLng> = emptyList()
)

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val torrentialRain = TorrentialRain()
    private val nveRepository = NVERepository(NVEDataSource(application))
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val placesClient = Places.createClient(application)
    private val sessionToken = AutocompleteSessionToken.newInstance()
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHazardMarkers()
        fetchFloodWarnings()
    }

    fun fetchSuggestions(query: String) {
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
                _uiState.update { it.copy(suggestions = suggestions) }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(suggestions = emptyList()) }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchCurrentLocation(onLocationFound: (LatLng) -> Unit) {
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location ->
                location?.let {
                    val userPosition = LatLng(it.latitude, it.longitude)
                    _uiState.update { state ->
                        state.copy(
                            markerPosition = userPosition,
                            searchQuery = "",
                            markerTitle = ""
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
                val latLngs = warnings.flatMap { warning ->
                    warning.municipalityList.mapNotNull { municipality ->
                        val lat = municipality.lat
                        val lon = municipality.lon
                        if (lat != null && lon != null) LatLng(lat, lon) else null
                    }
                }
                _uiState.update { it.copy(flomList = latLngs) }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error fetching flood warnings", e)
            }
        }
    }

    fun setMarkerPosition(position: LatLng) {
        _uiState.update { it.copy(markerPosition = position) }
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getMarkerPosition(): LatLng = _uiState.value.markerPosition

    fun onSearchedLocation(query: String, position: LatLng) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                markerTitle = query,
                markerPosition = position
            )
        }
    }

    fun onMapClick(latLng: LatLng) {
        _uiState.update {
            it.copy(
                markerPosition = latLng,
                markerTitle = ""
            )
        }
    }

    fun onMapReady() {
        _uiState.update { it.copy(isMapReady = true) }
    }

    fun vectorToBitMap(
        context: Context,
        vectorResId: Int,
        targetWidth: Int = 150,
        targetHeight: Int = 150
    ): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, targetWidth, targetHeight)
        val bitmap = createBitmap(targetWidth, targetHeight)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun loadHazardMarkers() {
        val context = getApplication<Application>().applicationContext
        val downpourMarkers = torrentialRain.readLatLngFromJson(context, "torrential_rain_stations.json")
        val windMarkers = torrentialRain.readLatLngFromJson(context, "high_wind_speed_stations.json")
        _uiState.update {
            it.copy(
                downpourList = downpourMarkers,
                extremeWindList = windMarkers
            )
        }
    }

    fun setInitialLocationFlag() {
        _uiState.update { it.copy(hasInitialLocationBeenSet = true) }
    }
}