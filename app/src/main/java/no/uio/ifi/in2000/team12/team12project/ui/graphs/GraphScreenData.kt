package no.uio.ifi.in2000.team12.team12project.ui.graphs

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType

/*
 * Needs to store all five climate types
 * Need get and set functions for them by climate type
 *
 */


/**
 * GraphScreenData class
 *
 * @param position: LatLng
 */
class GraphScreenData(position: LatLng) {

    private val climateMap = mutableMapOf<ClimateType, ClimateData>()

    fun setClimateData(climateType: ClimateType, climateData: ClimateData) {
        climateMap[climateType] = climateData
    }

    fun getClimateData(climateType: ClimateType): ClimateData? {
        return climateMap[climateType]
    }

    fun containsKey(climateType: ClimateType): Boolean {
        return climateMap.containsKey(climateType)
    }
}