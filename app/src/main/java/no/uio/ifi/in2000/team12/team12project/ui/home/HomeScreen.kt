package no.uio.ifi.in2000.team12.team12project.ui.home

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.data.frost.ObsStationsDataSource
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(homeScreenViewModel: HomeScreenViewModel = viewModel()) {


    val uiState by homeScreenViewModel.uiState.collectAsState()


    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val obsStationsDataSource : ObsStationsDataSource by remember {mutableStateOf(
        ObsStationsDataSource())}

    var searchQuery by remember { mutableStateOf("") }
    var searchedLocation by remember { mutableStateOf<LatLng?>(LatLng(59.9127, 10.7461)) }
    var markerTitle by remember {mutableStateOf("")}
    //var markerPosition by remember{mutableStateOf(LatLng(59.9127, 10.7461))}

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(59.9127, 10.7461), 10f)
    }

    fun searchLocation(query: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(query, 1)
        if (!results.isNullOrEmpty()) {
            val location = results[0]
            val latLng = LatLng(location.latitude, location.longitude)
            searchedLocation = latLng
            homeScreenViewModel.setMarkerPosition(latLng)
            markerTitle = query
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 20f)
        }
    }





    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                searchedLocation = latLng
                homeScreenViewModel.setMarkerPosition(latLng)
                searchQuery = ""
            }
        ) {
            val location = searchedLocation
            if (location != null) {
                Marker(
                    state = MarkerState(homeScreenViewModel.getMarkerPosition()),
                    title = searchQuery
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search location...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchLocation(searchQuery)
                        keyboardController?.hide()
                        homeScreenViewModel.getMarkerPosition()
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

            OutlinedButton(
                onClick = {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(59.9127, 10.7461),12f))
                    homeScreenViewModel.setMarkerPosition(LatLng(59.9127, 10.7461))

                },
                contentPadding = PaddingValues(8.dp),
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.my_location),
                    contentDescription = "home",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)

                )
            }
            Button(
                onClick = {
                    homeScreenViewModel.getNearestStationData()
                }
            ){
                Text("Get nearest station")
            }
        }

    }
}