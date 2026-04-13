package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.R
import java.util.Locale

@Composable
fun HomeScreen(homeScreenViewModel: HomeScreenViewModel = viewModel(),
               navHostController: NavHostController) {
    val uiState by homeScreenViewModel.uiState.collectAsState()



    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.markerPosition, 10f)
    }
    val scope = rememberCoroutineScope()

    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            @Suppress("MissingPermission")
            homeScreenViewModel.fetchCurrentLocation { pos ->
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(pos, 15f))
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    fun searchLocation(query: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(query, 1)
        if (!results.isNullOrEmpty()) {
            val location = results[0]
            val latLng = LatLng(location.latitude, location.longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 20f)
            homeScreenViewModel.onSearchedLocation(query, latLng)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { homeScreenViewModel.onMapReady() },
            onMapClick = { latLng ->
                homeScreenViewModel.onMapClick(latLng)  //updates marker position
                println(uiState.markerPosition)
            }
        ) {
            Marker(
                state = remember(uiState.markerPosition) { MarkerState(uiState.markerPosition) },
                title = uiState.markerTitle
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            TextField(
                value = uiState.searchQuery,    // value is read from uiState
                onValueChange = { homeScreenViewModel.setSearchQuery(it) }, // updates value of uiState.searchQuery
                placeholder = { Text("Search location...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchLocation(uiState.searchQuery)
                        keyboardController?.hide()
                        //println(uiState.markerPosition)
                    }
                ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                OutlinedButton(
                    modifier = Modifier.align(Alignment.BottomStart),
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
                    },
                    contentPadding = PaddingValues(8.dp),
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.outline_my_location_24),
                        contentDescription = "return to current position",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)

                    )
                }

                Button(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = {
                        navHostController.navigate("graph")
                    },

                    ) {
                    Text("Show me the numbers!")
                }
            }

        }
        if (!uiState.isMapReady){
            LoadingScreen()
        }

    }
}