package no.uio.ifi.in2000.team12.team12project.ui.graphs

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.frost.findMeanValue
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
    private val chartDataMap = mutableMapOf<Pair<ClimateType, Months>, ClimateChartData>()

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

    fun setAllMeans() {
        for (climateType in ClimateType.entries) {
            for (month in Months.entries) {
                val obsFromMonth = getObsFromMonth(month, climateMap[climateType]!!.climateObs)
                val mean = findMeanValue(obsFromMonth)
                setMean(climateType, month, mean)
            }
        }
    }

    fun getMean(climateType: ClimateType, month: Months): Double? {
        if (!meansMap.containsKey(Pair(climateType, month))) {
            Log.e("GraphScreenData", "No mean found for $climateType in $month")
        }
        return meansMap[Pair(climateType, month)]
    }

    fun containsKey(climateType: ClimateType, month: Months): Boolean {
        return meansMap.containsKey(Pair(climateType, month))
    }

    fun getClimateChartData(climateType: ClimateType, month: Months): ClimateChartData? {
        // Return cached or calculate if ClimateData exists
        val key = Pair(climateType, month)
        if (chartDataMap.containsKey(key)) return chartDataMap[key]

        val data = climateMap[climateType] ?: return null
        val obsFromMonth = getObsFromMonth(month, data.climateObs)
        val chartData = getYearAndData(obsFromMonth)
        setAllMeans()
        chartDataMap[key] = chartData
        return chartData
    }
}