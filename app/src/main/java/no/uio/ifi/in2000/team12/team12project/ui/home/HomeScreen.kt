package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.AdvancedMarkerOptions.CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.io.Files.map
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import java.util.Locale

@Composable
fun HomeScreen(homeScreenViewModel: HomeScreenViewModel = viewModel(),favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(),
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

    //variables with the changed location on the map and to ask if user wants to add location to favorites
    var askpermissonforlatlng by remember { mutableStateOf<LatLng?>(null)}
    var askforpermission by remember { mutableStateOf(false)}


    fun searchLocation(query: String) {
        val geocoder = Geocoder(context, Locale.getDefault()) //comment out and have it as a global variable. then have a local variable pointing to it, so it can be used for reverse geocoding
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

            //val onMapClick:(LatLng) -> Unit = { latlng ->
                homeScreenViewModel.onMapClick(latLng)  //updates marker position
                println(uiState.markerPosition)

                askpermissonforlatlng = latLng
                askforpermission = true

                /*
                //add as favorite. unsure if i should implement in a way where fscreenviewmodel isnt used here
                favoritesScreenViewModel.addFavoriteLocation(  //this works but it adds to favorites without asking
                    location = latLng,
                    name = "idk",  // gotta fix name of the place
                    address = " dontknow " //fix adress tooooooo
                )*/
            },
           // onMapClick = onMapClick //equals the variable so there wont be a need for onmapclick over, and to comment in val onmapclick
            //since the other one was over i tried to continue with the same way of implementation/logic used here
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId("48427ab8c098a917274af32e")
            }


        ) {
            //
            if(askforpermission == true && askpermissonforlatlng != null){
                AlertDialog(
                    onDismissRequest = {askforpermission = false},
                    title = {Text("favorites?")},
                    text = {Text("do you want this location added to your favorites?")},
                    confirmButton = {
                        TextButton( onClick = {
                            favoritesScreenViewModel.addFavoriteLocation(
                                location = askpermissonforlatlng !!,
                                name = "idk" , // gotta fix name of the place tho
                                address = "unsure" //fix adress tooooooo
                            )

                            askforpermission = false
                        }) {
                            Text("yessssssssssirrrr")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {askforpermission = false}) {
                            Text("Nosirrrrrrrrrr")
                        }
                    }
                )
            }
            //

            //
            Marker(
                state = remember(uiState.markerPosition) { MarkerState(uiState.markerPosition) },
                title = uiState.markerTitle,
                onClick = {println("marker clicked at ${uiState.markerPosition}") ; true}

            )

            uiState.downpourList.forEach {
                AdvancedMarker(
                    state = MarkerState(it),
                    contentDescription = "",
                    collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    icon = homeScreenViewModel.vectorToBitMap(LocalContext.current, R.drawable.outline_rainy_24),


                    )
            }
            uiState.extremeWindList.forEach {
                AdvancedMarker(
                    state = MarkerState(it),
                    contentDescription = "",
                    collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    icon = homeScreenViewModel.vectorToBitMap(LocalContext.current, R.drawable.baseline_air_24)
                )
            }
            uiState.flomList.forEach {
                AdvancedMarker(
                    state = MarkerState(it),
                    contentDescription = "",
                    collisionBehavior = OPTIONAL_AND_HIDES_LOWER_PRIORITY,
                    icon = homeScreenViewModel.vectorToBitMap(LocalContext.current, R.drawable.baseline_water_24),
                    //onClick = {}
                )
            }

        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = uiState.searchQuery,    // value is read from uiState
                    onValueChange = {
                        homeScreenViewModel.setSearchQuery(it)
                        //DENNE MÅ REAKTIVERES FOR Å FÅ FORSLAG PÅ SØK
                        //homeScreenViewModel.fetchSuggestions(it)
                    },
                    placeholder = { Text("Search location...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        //'autoCorrect = false
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            searchLocation(uiState.searchQuery)
                            keyboardController?.hide()

                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                       // .padding(16.dp)
                )

                if (uiState.suggestions.isNotEmpty() && uiState.searchQuery.isNotEmpty()){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ){
                        uiState.suggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchLocation(suggestion)
                                        homeScreenViewModel.clearSuggestions()
                                        keyboardController?.hide()
                                    }
                                    .padding(12.dp)
                            )
                            HorizontalDivider()
                        }
                    }
                }
                if (uiState.searchQuery.isNotEmpty() && uiState.suggestions.isEmpty()){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)

                    ) {
                        Text(
                            text = "Søket ga ingen treff",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
            }
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



/*
TODO
- Bedre symboler
- Collision handling - Check!
- Pop up
- Clean up, kankje lag egen klasse elns, så alle plasser kan ligge i samme liste
- API
- Tegne område rundt?





 */