package no.uio.ifi.in2000.team12.team12project.ui.home

import android.location.Geocoder
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import no.uio.ifi.in2000.team12.team12project.R
import java.util.Locale

@Composable
fun HomeScreen(homeScreenViewModel: HomeScreenViewModel = viewModel(),
               navHostController: NavHostController) {
    val uiState by homeScreenViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(59.9127, 10.7461), 10f)
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
            onMapClick = { latLng ->
                homeScreenViewModel.onMapClick(latLng)  //updates marker position
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        val temporaryReturnPosition = LatLng(59.9127, 10.7461)
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(
                                temporaryReturnPosition,
                                12f
                            )
                        )
                        homeScreenViewModel.setMarkerPosition(temporaryReturnPosition)
                        homeScreenViewModel.setSearchQuery("")

                    },
                    contentPadding = PaddingValues(8.dp),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.outline_my_location_24),  // Needs a better image
                        contentDescription = "return to current position",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)

                    )
                }

                Button(
                    onClick = {
                        navHostController.navigate("graph")
                    },

                    ) {
                    Text("Show me the numbers!")
                }
            }
        }

    }
}