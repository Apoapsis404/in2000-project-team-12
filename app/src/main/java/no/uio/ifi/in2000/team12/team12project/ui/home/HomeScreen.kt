package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
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
import no.uio.ifi.in2000.team12.team12project.BuildConfig
import no.uio.ifi.in2000.team12.team12project.R
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
                    homeScreenViewModel.setMarkerPosition(pos)
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

    val onMapOrMarkerClick: (LatLng) -> Unit = { latLng ->
        if (!isSearchFocused) {
            homeScreenViewModel.onMapClick(latLng)
        }
        localFocusManager.clearFocus()
        keyboardController?.hide()
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { homeScreenViewModel.onMapReady() },
            onMapClick = onMapOrMarkerClick,
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId(BuildConfig.MAPS_ID)
            },
            uiSettings = mapUiSettings,
        ) {
            // Position marker
            AdvancedMarker(
                state = remember(uiState.markerPosition) { MarkerState(uiState.markerPosition) },
                title = uiState.markerTitle,
                onClick = { true },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )

            // Render hazard markers based on filters
            HazardMarkers(
                show = uiState.showDownpour,
                list = uiState.downpourList,
                iconRes = R.drawable.outline_rainy_24,
                contentDescription = stringResource(R.string.cont_desc_downpour_marker),
                homeScreenViewModel = homeScreenViewModel,
                onMarkerClick = onMapOrMarkerClick
            )
            HazardMarkers(
                show = uiState.showExtremeWind,
                list = uiState.extremeWindList,
                iconRes = R.drawable.baseline_air_24,
                contentDescription = stringResource(R.string.cont_desc_wind_marker),
                homeScreenViewModel = homeScreenViewModel,
                onMarkerClick = onMapOrMarkerClick
            )
            HazardMarkers(
                show = uiState.showFlood,
                list = uiState.floodList,
                iconRes = R.drawable.baseline_water_24,
                contentDescription = stringResource(R.string.cont_desc_flood_marker),
                homeScreenViewModel = homeScreenViewModel,
                onMarkerClick = onMapOrMarkerClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .navigationBarsPadding()
        ) {
            SearchBox(
                uiState = uiState,
                isSearchFocused = isSearchFocused,
                onFocusChanged = { isSearchFocused = it },
                onQueryChange = {
                    homeScreenViewModel.fetchSuggestions(it)
                    homeScreenViewModel.setSearchQuery(it)
                                },
                onSearch = { query ->
                    homeScreenViewModel.searchLocation(query) {
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                            homeScreenViewModel.onMapClick(it)
                        }

                    }
                    keyboardController?.hide()
                    localFocusManager.clearFocus()
                },
                onSuggestionClick = { suggestion ->
                    homeScreenViewModel.searchLocation(suggestion) {
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
                            homeScreenViewModel.onMapClick(it)
                        }
                    }
                    homeScreenViewModel.clearSuggestions()
                    keyboardController?.hide()
                    localFocusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            MapControls(
                onToggleFilter = { homeScreenViewModel.toggleFilterPopup() },
                onReturnToLocation = {
                    val fineLocationGranted = android.content.pm.PackageManager.PERMISSION_GRANTED ==
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            )
                    if (fineLocationGranted) {
                        @Suppress("MissingPermission")
                        homeScreenViewModel.fetchCurrentLocation { position ->
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(position, 15f))
                                homeScreenViewModel.onMapClick(position)
                            }
                        }
                    } else {
                        locationPermissionRequest.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                onZoomIn = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } },
                onZoomOut = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } }
            )
        }

        if (!uiState.isMapReady) {
            LoadingScreen()
        }

        if (uiState.isFilterPopupVisible) {
            FilterDialog(
                uiState = uiState,
                onToggleDownpour = { homeScreenViewModel.toggleDownpourFilter() },
                onToggleWind = { homeScreenViewModel.toggleExtremeWindFilter() },
                onToggleFlood = { homeScreenViewModel.toggleFloodFilter() },
                onDismiss = { homeScreenViewModel.toggleFilterPopup() }
            )
        }

        if (uiState.showNoInternetDialog) {
            AlertDialog(
                onDismissRequest = { homeScreenViewModel.dismissNoInternetDialog() },
                title = { Text(stringResource(R.string.no_internet_title)) },
                text = { Text(stringResource(R.string.no_internet_navigation_message)) },
                confirmButton = {
                    TextButton(onClick = { homeScreenViewModel.dismissNoInternetDialog() }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}

@Composable
fun HazardMarkers(
    show: Boolean,
    list: List<LatLng>,
    iconRes: Int,
    contentDescription: String,
    homeScreenViewModel: HomeScreenViewModel,
    onMarkerClick: (LatLng) -> Unit = {}
) {
    if (show) {
        val context = LocalContext.current
        list.forEach { latLng ->
            AdvancedMarker(
                state = remember { MarkerState(latLng) },
                contentDescription = contentDescription,
                collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                icon = homeScreenViewModel.vectorToBitMap(context, iconRes),
                onClick = {
                    onMarkerClick(latLng)
                    true // Consume click to prevent camera focus on hazard markers
                }
            )
        }
    }
}

@Composable
fun SearchBox(
    uiState: HomeScreenUiState,
    isSearchFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSuggestionClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = uiState.searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search_location)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(uiState.searchQuery) }),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) }
        )
        if (isSearchFocused && uiState.searchQuery.isNotEmpty()) {
            if (uiState.suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    uiState.suggestions.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionClick(suggestion) }
                                .padding(12.dp)
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_result),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun MapControls(
    onToggleFilter: () -> Unit,
    onReturnToLocation: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FilledIconButton(
            onClick = onToggleFilter,
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.cont_desc_filter_hazards))
        }

        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
            FilledIconButton(onClick = onReturnToLocation) {
                Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.cont_desc_return_to_current_position))
            }
            FilledIconButton(onClick = onZoomIn) {
                Icon(Icons.Outlined.AddCircleOutline, contentDescription = stringResource(R.string.cont_desc_zoom_in))
            }
            FilledIconButton(onClick = onZoomOut) {
                Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = stringResource(R.string.cont_desc_zoom_out))
            }
        }
    }
}

@Composable
fun FilterDialog(
    uiState: HomeScreenUiState,
    onToggleDownpour: () -> Unit,
    onToggleWind: () -> Unit,
    onToggleFlood: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter_hazards),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                FilterItem(
                    label = stringResource(R.string.downpour),
                    checked = uiState.showDownpour,
                    onCheckedChange = { onToggleDownpour() }
                )
                FilterItem(
                    label = stringResource(R.string.extreme_wind),
                    checked = uiState.showExtremeWind,
                    onCheckedChange = { onToggleWind() }
                )
                FilterItem(
                    label = stringResource(R.string.flood),
                    checked = uiState.showFlood,
                    onCheckedChange = { onToggleFlood() }
                )
            }
        }
    }
}

@Composable
fun FilterItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}