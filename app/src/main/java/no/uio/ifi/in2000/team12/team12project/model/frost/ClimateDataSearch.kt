package no.uio.ifi.in2000.team12.team12project.model.frost

import com.google.android.gms.maps.model.LatLng

// TODO: Find a better name for this!
data class ClimateDataSearch(
    val position: LatLng,
    val climateType: ClimateType,
    val referenceTime: String,
    val climateObs: List<WeatherObs>,
)
