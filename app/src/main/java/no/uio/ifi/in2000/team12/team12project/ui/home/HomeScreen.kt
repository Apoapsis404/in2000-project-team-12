package no.uio.ifi.in2000.team12.team12project.ui.home

import android.Manifest
import android.location.Geocoder
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.ui.favorites.FavoritesScreenViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = viewModel(),
    favoritesScreenViewModel: FavoritesScreenViewModel = viewModel(),
    navHostController: NavHostController,
    modifier : Modifier = Modifier
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.markerPosition, 10f)
    }
    val mapUiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }

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
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(query, 1)
        if (!results.isNullOrEmpty()) {
            val location = results[0]
            val latLng = LatLng(location.latitude, location.longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 20f)
            homeScreenViewModel.onSearchedLocation(query, latLng)
        }
    }


    Box(modifier = modifier.fillMaxSize()) {

        GoogleMap(
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { homeScreenViewModel.onMapReady() },
            onMapClick = { latLng ->

                homeScreenViewModel.onMapClick(latLng)  //updates marker position
                println(uiState.markerPosition)

                askpermissonforlatlng = latLng

            },
            googleMapOptionsFactory = {
                GoogleMapOptions().mapId( "48427ab8c098a917274af32e")
            },
            uiSettings = mapUiSettings,
        ) {
            //
            if(askforpermission && askpermissonforlatlng != null){
                AlertDialog(
                    onDismissRequest = {askforpermission = false},
                    title = {Text(stringResource(R.string.favorites_popup_title))},
                    text = {Text(stringResource(R.string.favorites_popup_question))},
                    confirmButton = {
                        TextButton(
                            onClick = {
                            scope.launch {
                                val tempname = "temp_${System.currentTimeMillis()}" //temporary name so it is easier to tell apart the diffrent adresses in order to rename

                                val (name, address) = getNameandAdress( //name is no longer used
                                    context,
                                    askpermissonforlatlng!!.latitude,
                                    askpermissonforlatlng!!.longitude
                                )
                                //navigates to favorite screen
                                navHostController.navigate("favorites")

                                favoritesScreenViewModel.addFavoriteLocation(
                                location = askpermissonforlatlng!!,
                                name = tempname , //"add name",//name.toString(), //add apart of the address. will later on add ability to change the name
                                address = address.toString()
                            )

                            }
                            askforpermission = false
                        }) {
                            Text(stringResource(R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {askforpermission = false}) {
                            Text(stringResource(R.string.no))
                        }
                    }
                )
            }
            AdvancedMarker(
                state = remember(uiState.markerPosition) { MarkerState(uiState.markerPosition) },
                title = uiState.markerTitle ,
                onClick = {
                    true
                    //println("marker clicked at ${uiState.markerPosition}") ; true}
                },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
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
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                TextField(
                    value = uiState.searchQuery,    // value is read from uiState
                    onValueChange = {
                        homeScreenViewModel.setSearchQuery(it)
                        //DENNE MÅ REAKTIVERES FOR Å FÅ FORSLAG PÅ SØK
                        // homeScreenViewModel.fetchSuggestions(it)
                    },
                    placeholder = { Text(stringResource(R.string.search_location)) },
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
                    modifier = modifier
                        .fillMaxWidth()
                       // .padding(16.dp)
                )

                if (uiState.suggestions.isNotEmpty() && uiState.searchQuery.isNotEmpty()){
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ){
                        uiState.suggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = modifier
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
                modifier = modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column{
                    // Favorite button
                    FilledIconButton(
                        onClick = {
                            askpermissonforlatlng = uiState.markerPosition
                            askforpermission = true
                        }
                    ){
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite button",
                        )
                    }
                    // My location button
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
                        },
                        //contentPadding = PaddingValues(8.dp),
                        //shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "return to my location",
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = "zoom in",
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveCircleOutline,
                            contentDescription = "zoom out",
                        )
                    }


                }
            }

        }
        if (!uiState.isMapReady){
            LoadingScreen()
        }

    }
}

suspend fun getNameandAdress(
    context: android.content.Context,
    //geocoder: Geocoder,
    lat: Double,
    long: Double
): Pair<String?,String?> = withContext(Dispatchers.IO){
    try {
        val geocoder = Geocoder(context,Locale.getDefault())
        val locationdetails = geocoder.getFromLocation(lat,long,1)
        if(!locationdetails.isNullOrEmpty()){
            val locationobject = locationdetails[0]
            val name = locationobject.featureName
            val address = locationobject.getAddressLine(0)
            return@withContext name to address
        }
    }catch (e : Exception){
        e.printStackTrace()
    }
    null to null

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