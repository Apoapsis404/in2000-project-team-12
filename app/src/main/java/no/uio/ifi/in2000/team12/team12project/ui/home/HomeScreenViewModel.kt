package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateHazard
import no.uio.ifi.in2000.team12.team12project.data.local.AppDatabase
import no.uio.ifi.in2000.team12.team12project.data.local.SettingsEntity
import no.uio.ifi.in2000.team12.team12project.data.nve.NVEDataSource
import no.uio.ifi.in2000.team12.team12project.data.nve.NVERepository
import no.uio.ifi.in2000.team12.team12project.utils.NetworkUtils
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages
import java.util.Locale
import kotlin.coroutines.resume
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation

data class HomeScreenUiState(
    val markerPosition: LatLng = LatLng(59.9127, 10.7461),
    val hasInitialLocationBeenSet: Boolean = false,
    val address: String = "",
    val searchQuery: String = "",
    val markerTitle: String = "",
    val isMapReady: Boolean = false,
    val suggestions: List<String> = emptyList(),
    val downpourList: List<LatLng> = emptyList(),
    val extremeWindList: List<LatLng> = emptyList(),
    val floodList: List<LatLng> = emptyList(),
    val showDownpour: Boolean = true,
    val showExtremeWind: Boolean = true,
    val showFlood: Boolean = true,
    val isFilterPopupVisible: Boolean = false,
    val showNoInternetDialog: Boolean = false
)

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val climateHazard: ClimateHazard = ClimateHazard()
    private val nveRepository = NVERepository(NVEDataSource(application))
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private val placesClient = Places.createClient(application)

    private val settingsDao = AppDatabase.getDatabase(application).settingsDao()
    private var currentSettings: SettingsEntity = SettingsEntity()

    private var sessionToken = AutocompleteSessionToken.newInstance()
    private val bitmapCache = mutableMapOf<Int, BitmapDescriptor>()

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHazardMarkers()
        fetchFloodWarnings()
        observeSettings()
    }

    // Observes settings from the database and updates the uiState
    private fun observeSettings() {
        viewModelScope.launch {
            settingsDao.getSettings().collect { settings ->
                settings?.let { s ->
                    currentSettings = s
                    _uiState.update {
                        it.copy(
                            showDownpour = s.showDownpour,
                            showExtremeWind = s.showExtremeWind,
                            showFlood = s.showFlood
                        )
                    }
                }
            }
        }
    }

    // Updates the filter settings in the database
    private fun updateFilterSetting(
        showDownpour: Boolean = currentSettings.showDownpour,
        showExtremeWind: Boolean = currentSettings.showExtremeWind,
        showFlood: Boolean = currentSettings.showFlood
    ) {
        viewModelScope.launch {
            settingsDao.saveSettings(
                currentSettings.copy(
                    showDownpour = showDownpour,
                    showExtremeWind = showExtremeWind,
                    showFlood = showFlood
                )
            )
        }
    }

    // Fetches suggestions from google places based on the query in the TextField
    fun fetchSuggestions(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            _uiState.update { it.copy(showNoInternetDialog = true) }
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
    // Clears suggestions from the text field, used when a suggestion is selected
    fun clearSuggestions() {
        _uiState.update { it.copy(suggestions = emptyList()) }
        sessionToken = AutocompleteSessionToken.newInstance()
    }

    // Fetches the location of the device if the user has given location permission
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

    // Fetches historical flood data from NVE and updates the uiState with flood warnings
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
                _uiState.update { it.copy(floodList = latLngs) }
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error fetching flood warnings", e)
            }
        }
    }

    // Updates the marker position in the uiState
    fun setMarkerPosition(position: LatLng) {
        _uiState.update { it.copy(markerPosition = position) }
        updateAddress(position)
    }


    // Translates the position into an address using Geocoder
    private suspend fun getAddressFromLatLng(lat: Double, long: Double): String{
        try {
            val geocoder = Geocoder(getApplication(), Locale.getDefault())
            val locationDetails = geocoder.getFromLocation(lat,long,1)
            if(!locationDetails.isNullOrEmpty()){
                val locationObject = locationDetails[0]
                val address = locationObject.getAddressLine(0)
                return withContext(Dispatchers.IO) {address}
            }else{
                return withContext(Dispatchers.IO) {""}
            }
        }catch (e : Exception){
            e.printStackTrace()
            return withContext(Dispatchers.IO) {""}
        }
    }

    // Updates the address in the uiState
    fun updateAddress(position: LatLng){
        viewModelScope.launch {
            val address = getAddressFromLatLng(position.latitude, position.longitude)
            _uiState.update { it.copy(address = address) }
        }
    }

    // Updates the search query in the uiState
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Updates the search query and marker title in the uiState
    fun onSearchedLocation(query: String, position: LatLng) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                markerTitle = query,
                markerPosition = position
            )
        }
        sessionToken = AutocompleteSessionToken.newInstance()
    }

    // Searches for a location using Geocoder and updates the map and marker position
    fun searchLocation(query: String, onLocationFound: (LatLng) -> Unit) {
        if (!NetworkUtils.isNetworkAvailable(getApplication())) {
            _uiState.update { it.copy(showNoInternetDialog = true) }
            return
        }
        viewModelScope.launch {
            val latLng = getLatLngFromLocationName(getApplication(), query)
            latLng?.let {
                onSearchedLocation(query, it)
                onLocationFound(it)
            }
        }
    }

    private suspend fun getLatLngFromLocationName(
        context: Context,
        query: String
    ): LatLng? = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(query, 1) { addresses ->
                    continuation.resume(
                        addresses.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                    )
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, 1)?.firstOrNull()?.let {
                LatLng(it.latitude, it.longitude)
            }
        }
    }

    // Updates the marker position and title in the uiState based on mapClicks
    fun onMapClick(latLng: LatLng) {
        _uiState.update {
            it.copy(
                markerPosition = latLng,
                markerTitle = ""
            )
        }
        updateAddress(latLng)
    }
    // Updates the isMapReady in the uiState, used to end displaying of the loading screen
    fun onMapReady() {
        _uiState.update { it.copy(isMapReady = true) }
    }

    // Converts a vector drawable to a bitmap descriptor
    fun vectorToBitMap(
        context: Context,
        vectorResId: Int,
        targetWidth: Int = 150,
        targetHeight: Int = 150
    ): BitmapDescriptor? {
        bitmapCache[vectorResId]?.let { return it }
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, targetWidth, targetHeight)
        val bitmap = createBitmap(targetWidth, targetHeight)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap).also {
            bitmapCache[vectorResId] = it
        }
    }

    // Updates the uiState with the position of hazard markers
    private fun loadHazardMarkers() {
        val context = getApplication<Application>().applicationContext
        val downpourMarkers = climateHazard.readLatLngFromJson(context, "torrential_rain_stations.json")
        val windMarkers = climateHazard.readLatLngFromJson(context, "high_wind_speed_stations.json")
        _uiState.update {
            it.copy(
                downpourList = downpourMarkers,
                extremeWindList = windMarkers
            )
        }
    }

    // Updates the uiState if the user has given location permission
    fun setInitialLocationFlag() {
        _uiState.update { it.copy(hasInitialLocationBeenSet = true) }
    }

    // Toggle filter for downpour markers
    fun toggleDownpourFilter() {
        updateFilterSetting(showDownpour = !uiState.value.showDownpour)
    }

    // Toggle filter for extreme wind markers
    fun toggleExtremeWindFilter() {
        updateFilterSetting(showExtremeWind = !uiState.value.showExtremeWind)
    }

    // Toggle filter for flood markers
    fun toggleFloodFilter() {
        updateFilterSetting(showFlood = !uiState.value.showFlood)
    }

    // Toggles the visibility of the filter popup
    fun toggleFilterPopup() {
        _uiState.update { it.copy(isFilterPopupVisible = !it.isFilterPopupVisible) }
    }

    // Dismisses the no internet dialog
    fun dismissNoInternetDialog() {
        _uiState.update { it.copy(showNoInternetDialog = false) }
    }
}