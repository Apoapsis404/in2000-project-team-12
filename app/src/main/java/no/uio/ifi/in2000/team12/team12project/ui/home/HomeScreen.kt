package no.uio.ifi.in2000.team12.team12project.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MarkerState.Companion.invoke
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HomeScreen(){
    val oslo = LatLng(59.9127, 10.7461)
    val osloState = MarkerState(position = oslo)
    val cameraPosition = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(oslo, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPosition,
    ) {
        Marker(
            state = osloState,
            title = "Oslo er her"
        )
    }
}