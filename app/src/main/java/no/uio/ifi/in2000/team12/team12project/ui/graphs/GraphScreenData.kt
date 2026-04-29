package no.uio.ifi.in2000.team12.team12project.ui.graphs

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.frost.getObsFromMonth
import no.uio.ifi.in2000.team12.team12project.data.frost.getYearAndData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months

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

    fun getClimateChartData(climateType: ClimateType, month: Months): ClimateChartData {
        val obsFromMonth = getObsFromMonth(month, climateMap[climateType]!!.climateObs)
        return getYearAndData(climateMap[climateType]!!.climateObs)
    }
}