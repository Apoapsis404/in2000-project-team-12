package no.uio.ifi.in2000.team12.team12project.model.favorites

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.LatLng

data class FavoriteLocation(
    val coordinates: LatLng,
    val name: String,
    val address : String,
    val icon: ImageVector? = null
)
