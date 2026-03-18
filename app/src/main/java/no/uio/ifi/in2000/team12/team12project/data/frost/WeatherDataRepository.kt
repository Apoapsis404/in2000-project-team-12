package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs

class WeatherDataRepository {

    private val weatherDataSource = WeatherDataSource()

    private val weatherObsList = mutableListOf<WeatherObs>()

    suspend fun getWeatherData(sources: String, elementIds: String, referenceTime: String, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<WeatherObs> {
        weatherObsList.clear()
        weatherObsList.addAll(weatherDataSource.getWeatherData(sources, elementIds, referenceTime, fields))
        return weatherObsList
    }

    fun getObsFromYear(year: Int): List<WeatherObs> {
        if (weatherObsList.isEmpty()) {
            return emptyList()
        }
        val obsFromYear = mutableListOf<WeatherObs>()
        for (obs in weatherObsList) {
            if (isFromYear(obs.referenceTime, year)) {
                obsFromYear.add(obs)
            }
        }
        return obsFromYear
    }

    fun getObsFromMonth(month: Months): List<WeatherObs> {
        if (weatherObsList.isEmpty()) {
            return emptyList()
        }
        val obsFromMonth = mutableListOf<WeatherObs>()
        for (obs in weatherObsList) {
            if (isFromMonth(obs.referenceTime, month.value)) {
                obsFromMonth.add(obs)
            }
        }
        return obsFromMonth
    }

    fun findMeanValue(weatherObsList: List<WeatherObs>): Double {
        var mean = 0.0
        var obsMean = 0.0
        for (obs in weatherObsList) {
            obsMean = 0.0
            for (detail in obs.observations) {
                obsMean += detail.value
            }
            obsMean /= obs.observations.size
            mean += obsMean
        }
        return mean / weatherObsList.size
    }
}