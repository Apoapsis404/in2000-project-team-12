package no.uio.ifi.in2000.team12.team12project.ui.graphs

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.frost.getObsFromMonth
import no.uio.ifi.in2000.team12.team12project.data.frost.getYearAndData
import no.uio.ifi.in2000.team12.team12project.model.ClimateChartData
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
    private val meansMap = mutableMapOf<Pair<ClimateType, Months>, Double>()

    fun setClimateData(climateType: ClimateType, climateData: ClimateData) {
        climateMap[climateType] = climateData
    }

    fun getClimateData(climateType: ClimateType): ClimateData? {
        return climateMap[climateType]
    }

    fun containsKey(climateType: ClimateType): Boolean {
        return climateMap.containsKey(climateType)
    }

    fun setMean(climateType: ClimateType, month: Months, mean: Double) {
        meansMap[Pair(climateType, month)] = mean
    }

    fun getMean(climateType: ClimateType, month: Months): Double? {
        return meansMap[Pair(climateType, month)]
    }

    fun containsKey(climateType: ClimateType, month: Months): Boolean {
        return meansMap.containsKey(Pair(climateType, month))
    }


    /*
     * Needs to be rewritten
     * Store which month it is from
     * And if it asks for same month, return same data
     * if not update climate chart data for all climate types at once
     * store the mean values separately
     */
    fun getClimateChartData(climateType: ClimateType, month: Months): ClimateChartData {
        val obsFromMonth = getObsFromMonth(month, climateMap[climateType]!!.climateObs)
        return getYearAndData(climateMap[climateType]!!.climateObs)
    }
}