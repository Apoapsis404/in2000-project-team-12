package no.uio.ifi.in2000.team12.team12project.data.frost

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.data.exceptions.BadRequestException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoDataException
import no.uio.ifi.in2000.team12.team12project.data.exceptions.UnauthorizedException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.WeatherObs



class OWN_WeatherDataRepository {

    private val weatherDataSource = WeatherDataSource()
    private val stationRepository = ObsStationRepository()
    private val weatherObsList = mutableListOf<WeatherObs>()

    private val MAX_RELEVANT_STATION_COUNT = 25
    private val STATION_COUNT_INCREMENT = 5

    suspend fun getWeatherData(position: LatLng, climateType: ClimateType, referenceTime: String, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<WeatherObs> {
        weatherObsList.clear()
        val stations = stationRepository.getNearestStation(position.latitude, position.longitude, climateType)
        val source = stations.firstOrNull()?.id ?: throw NoAvailableTimeSeriesException("No nearby stations found")
        weatherObsList.addAll(weatherDataSource.getWeatherData(source, climateType.elementId, referenceTime, fields))
        return weatherObsList
    }

    suspend fun getWeatherDataFailing(position: LatLng, climateType: ClimateType, referenceTime: String, minCount: Int = 0, maxCount: Int = 5, fields: String = "sourceId,referenceTime,elementId,unit,value"): List<WeatherObs> {
        weatherObsList.clear()
        val sources = stationRepository.getNearestStation(position.latitude, position.longitude, climateType, maxCount = maxCount)
        
        for (source in sources.drop(minCount)) {
            try {
                weatherObsList.addAll(
                    weatherDataSource.getWeatherData(
                        source.id,
                        climateType.elementId,
                        referenceTime,
                        fields
                    )
                )
            } catch (e: NoAvailableTimeSeriesException) {
                // Skip this source and try the next one
                continue
            }catch (e: NoDataException){
                continue
            }catch (e: Exception){
                Log.e("OWN_WeatherDataRepository", "Error getting weather data: ${e.message}")
                throw  e
            }
            stationRepository.addStationToCache(source, position, climateType)
            break
        }

        if (weatherObsList.isEmpty()) {
            if (maxCount < MAX_RELEVANT_STATION_COUNT) {
                return getWeatherDataFailing(position, climateType, referenceTime, minCount = maxCount, maxCount = maxCount + STATION_COUNT_INCREMENT)
            } else {
                throw NoAvailableTimeSeriesException("No available time series")
            }
        }


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
        var obsMean : Double
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

    fun findMeanObs(weatherObs: WeatherObs): Double {
        var obsMean = 0.0
        for (detail in weatherObs.observations) {
            obsMean += detail.value
        }
        return obsMean / weatherObs.observations.size
    }

    fun getYearAndData(weatherObsList: List<WeatherObs>): WeatherChartData {
        val years = mutableListOf<Double>()
        val data = mutableListOf<Double>()

        for (obs in weatherObsList) {
            val year = obs.referenceTime.substring(0, 4).toDouble()
            val mean = findMeanObs(obs)
            years.add(year)
            data.add(mean)
        }
        return WeatherChartData(years, data)
    }
}
