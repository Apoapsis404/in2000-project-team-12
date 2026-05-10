package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.AdvancedMarkerOptions.CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team12.team12project.R
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val localFocusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.markerPosition, 10f)
    }
    val mapUiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            if (!homeScreenViewModel.uiState.value.hasInitialLocationBeenSet) {
                @Suppress("MissingPermission")
                homeScreenViewModel.fetchCurrentLocation { pos ->
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(pos, 15f))
                    homeScreenViewModel.onMapClick(pos)
                    homeScreenViewModel.setInitialLocationFlag()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun searchLocation(query: String) {
        scope.launch {
            val latLng = getLatLngFromLocationName(context, query)
            latLng?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                homeScreenViewModel.onSearchedLocation(query, it)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { homeScreenViewModel.onMapReady() },
            onMapClick = { latLng ->
                homeScreenViewModel.onMapClick(latLng)
                localFocusManager.clearFocus()
                keyboardController?.hide()
            },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId("48427ab8c098a917274af32e")
            },
            uiSettings = mapUiSettings,
        ) {
            AdvancedMarker(
                state = remember(uiState.markerPosition) { MarkerState(uiState.markerPosition) },
                title = uiState.markerTitle,
                onClick = { true },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
            uiState.downpourList.forEach {
                AdvancedMarker(
                    state = remember { MarkerState(it) },
                    contentDescription = stringResource(R.string.cont_desc_downpour_marker),
                    collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    icon = homeScreenViewModel.vectorToBitMap(
                        LocalContext.current,
                        R.drawable.outline_rainy_24
                    )
                )
            }
            uiState.extremeWindList.forEach {
                AdvancedMarker(
                    state = remember { MarkerState(it) },
                    contentDescription = stringResource(R.string.cont_desc_wind_marker),
                    collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    icon = homeScreenViewModel.vectorToBitMap(
                        LocalContext.current,
                        R.drawable.baseline_air_24
                    )
                )
            }
            uiState.flomList.forEach {
                AdvancedMarker(
                    state = remember { MarkerState(it) },
                    contentDescription = stringResource(R.string.cont_desc_flood_marker),
                    collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    icon = homeScreenViewModel.vectorToBitMap(
                        LocalContext.current,
                        R.drawable.baseline_water_24
                    )
                )
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
                .navigationBarsPadding()
        ) {
            Column(modifier = modifier.fillMaxWidth()) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { homeScreenViewModel.setSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.search_location)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            searchLocation(uiState.searchQuery)
                            keyboardController?.hide()
                            localFocusManager.clearFocus()
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = modifier
                        .fillMaxWidth()
                        .onFocusChanged { isSearchFocused = it.isFocused }
                )

                if (uiState.suggestions.isNotEmpty() && uiState.searchQuery.isNotEmpty() && isSearchFocused) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        uiState.suggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchLocation(suggestion)
                                        homeScreenViewModel.clearSuggestions()
                                        keyboardController?.hide()
                                        localFocusManager.clearFocus()
                                    }
                                    .padding(12.dp)
                            )
                            HorizontalDivider()
                        }
                    }
                }

                if (uiState.searchQuery.isNotEmpty() && uiState.suggestions.isEmpty() && isSearchFocused) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        Text(
                            text = stringResource(R.string.no_result),
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = modifier.weight(1f))

            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column {
                    FilledIconButton(
                        onClick = {
                            val fineLocationGranted =
                                android.content.pm.PackageManager.PERMISSION_GRANTED ==
                                        androidx.core.content.ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                            if (fineLocationGranted) {
                                @Suppress("MissingPermission")
                                homeScreenViewModel.fetchCurrentLocation { position ->
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(position, 15f)
                                        )
                                    }
                                }
                            } else {
                                locationPermissionRequest.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.cont_desc_return_to_current_position)
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = stringResource(R.string.cont_desc_zoom_in)
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveCircleOutline,
                            contentDescription = stringResource(R.string.cont_desc_zoom_out)
                        )
                    }
                }
            }
        }

        if (!uiState.isMapReady) {
            LoadingScreen()
        }
    }
}

suspend fun getLatLngFromLocationName(
    context: android.content.Context,
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

suspend fun getNameAndAddress(
    context: android.content.Context,
    lat: Double,
    lon: Double
): Pair<String?, String?> = withContext(Dispatchers.IO) {
    val geocoder = Geocoder(context, Locale.getDefault())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(lat, lon, 1) { addresses ->
                val address = addresses.firstOrNull()
                continuation.resume(address?.featureName to address?.getAddressLine(0))
            }
        }
    } else {
        try {
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(lat, lon, 1)
            val address = results?.firstOrNull()
            address?.featureName to address?.getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
            null to null
        }
    }
}