package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs


class WeatherDataRepository {

    private val climateDataSource = ClimateDataSource()
    private val stationRepository = ObsStationRepository()
    private val climateObsList = mutableListOf<ClimateObs>()

    suspend fun getWeatherData(position: LatLng, climateType: ClimateType, referenceTime: String, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<ClimateObs> {
        climateObsList.clear()
        val stations = stationRepository.getNearestStation(position.latitude, position.longitude, climateType)
        val source = stations.firstOrNull()?.id ?: throw NoAvailableTimeSeriesException("No nearby stations found")
        climateObsList.addAll(climateDataSource.getClimateData(source, climateType.elementId, referenceTime, fields))
        return climateObsList
    }

    suspend fun getWeatherDataFailing(position: LatLng, climateType: ClimateType, referenceTime: String, minCount: Int = 0, maxCount: Int = 5, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<ClimateObs> {
        climateObsList.clear()
        val sources = stationRepository.getNearestStation(position.latitude, position.longitude, climateType, maxDist = 5.0, maxCount = maxCount)
        
        for (source in sources.drop(minCount)) {
            try {
                climateObsList.addAll(
                    climateDataSource.getClimateData(
                        source.id,
                        climateType.elementId,
                        referenceTime,
                        fields
                    )
                )
            } catch (e: NoAvailableTimeSeriesException) {
                // Skip this source and try the next one
                continue
            } // TODO: Handle other exceptions
            stationRepository.addStationToCache(source, position, climateType)
            break
        }

        if (climateObsList.isEmpty()) {
            if (maxCount < 25) {
                return getWeatherDataFailing(position, climateType, referenceTime, minCount = maxCount, maxCount = maxCount + 5)
            } else {
                throw NoAvailableTimeSeriesException("No available time series")
            }
        }


        return climateObsList
    }

    fun getObsFromYear(year: Int): List<ClimateObs> {
        if (climateObsList.isEmpty()) {
            return emptyList()
        }
        val obsFromYear = mutableListOf<ClimateObs>()
        for (obs in climateObsList) {
            if (isFromYear(obs.referenceTime, year)) {
                obsFromYear.add(obs)
            }
        }
        return obsFromYear
    }

    fun getObsFromMonth(month: Months): List<ClimateObs> {
        if (climateObsList.isEmpty()) {
            return emptyList()
        }
        val obsFromMonth = mutableListOf<ClimateObs>()
        for (obs in climateObsList) {
            if (isFromMonth(obs.referenceTime, month.value)) {
                obsFromMonth.add(obs)
            }
        }
        return obsFromMonth
    }

    fun findMeanValue(climateObsList: List<ClimateObs>): Double {
        var mean = 0.0
        var obsMean : Double
        for (obs in climateObsList) {
            obsMean = 0.0
            for (detail in obs.observations) {
                obsMean += detail.value
            }
            obsMean /= obs.observations.size
            mean += obsMean
        }
        return mean / climateObsList.size
    }

    fun findMeanObs(climateObs: ClimateObs): Double {
        var obsMean = 0.0
        for (detail in climateObs.observations) {
            obsMean += detail.value
        }
        return obsMean / climateObs.observations.size
    }

    fun getYearAndData(climateObsList: List<ClimateObs>): ClimateChartData {
        val years = mutableListOf<Double>()
        val data = mutableListOf<Double>()

        for (obs in climateObsList) {
            val year = obs.referenceTime.substring(0, 4).toDouble()
            val mean = findMeanObs(obs)
            years.add(year)
            data.add(mean)
        }
        return ClimateChartData(years, data)
    }
}
