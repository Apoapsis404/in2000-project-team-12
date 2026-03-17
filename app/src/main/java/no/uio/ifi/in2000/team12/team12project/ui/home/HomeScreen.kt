package no.uio.ifi.in2000.team12.team12project.ui.home

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by remember { mutableStateOf("") }
    var searchedLocation by remember { mutableStateOf<LatLng?>(null) }
    var markerTitle by remember {mutableStateOf("")}

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
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 12f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                searchedLocation = latLng
                markerTitle = "Valgt posisjon"
                searchQuery = ""
            }
        ) {
            val location = searchedLocation
            if (location != null) {
                Marker(
                    state = MarkerState(position = location),
                    title = searchQuery
                )
            }
        }

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
                }
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}