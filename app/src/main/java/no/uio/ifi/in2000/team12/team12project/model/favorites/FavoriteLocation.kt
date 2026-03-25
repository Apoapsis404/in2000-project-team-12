package no.uio.ifi.in2000.team12.team12project.model.favorites

import com.google.android.gms.maps.model.LatLng

data class FavoriteLocation(
    val coordinates: LatLng,
    val name: String,
)
