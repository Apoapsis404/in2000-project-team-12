package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType
import kotlin.math.min

const val durationElement = "sum(duration_of_precipitation PT1H)"
const val amountElement = "sum(precipitation_amount P1D)"

class TorrentialRain(
    private val climateDataRepository: ClimateDataRepository = ClimateDataRepository()
) {

    /*
     * Torrential Rain is calculated as follows:
     * Amount precipitation in a day / sum of minutes it has rained that day
     * sum of minutes is found by adding up the duration in minutes individual hours of the day (annoying!)
     */
    private val torrentialRainRate = 7.5 // mm/hr (source: https://www.baranidesign.com/faq-articles/2020/1/19/rain-rate-intensity-classification)

    private val instancesPerYear = 5 // How many instances of torrential rain should be minimum in a year for it to be considered a threat

    /**
     * Found that some observation where the duration of precipitation
     * where really low, especially compared to the precipitation amount
     * so limits the minimum duration of precipitation to 30 minutes
     */
    private val minPrecipitationDuration = 30 // minutes

    suspend fun getClimateData(sourceId: String, monthsBack: UInt, elementId: String): List<ClimateObs> {
        val obs = climateDataRepository.getClimateData(sourceId, elementId,constructTimeSeries(monthsBack))
        return obs
    }

    suspend fun needsName(sourceId: String): Boolean {
        var durationTimeSeries = ""
        var amountTimeSeries = ""

        try {
            durationTimeSeries = climateDataRepository.getClimateTimeSeries(sourceId, durationElement).substring(0, 10)
            amountTimeSeries = climateDataRepository.getClimateTimeSeries(sourceId, amountElement).substring(0, 10)
        } catch (e: NoAvailableTimeSeriesException) {
            println("No time series found for $sourceId, skipping")
            return false
        }

        println("Time series found for $sourceId")

        val months = min(min(monthsSince(durationTimeSeries), monthsSince(amountTimeSeries)).toUInt(), 120u)
        println("Months since: $months")



        val duration = getClimateData(sourceId, months, durationElement)
        val amount = getClimateData(sourceId, months, amountElement)

        val rainfall = calculateTorrentialRain(duration, amount) / (if (months > 12u) (months / 12u).toInt() else 1)
        println("Rainfall: $rainfall")

        return rainfall >= instancesPerYear
    }

    fun calculateTorrentialRain(duration: List<ClimateObs>, amount: List<ClimateObs>): Int {
        var res = 0

        // Keeps track of the index into the duration list
        var durationIndex = 0

        amount.forEach { amountObs ->
            val date = amountObs.referenceTime.substring(0, 10)
            val precipAmount = findMaxObs(amountObs)

            val sum = sumPrecipitationDuration(date, duration, durationIndex)

            durationIndex = sum.second

            if (sum.first == 0.0 || precipAmount == 0.0 || sum.first < minPrecipitationDuration) {
                return@forEach
            }

            val rate = precipAmount / (sum.first / 60)
            if (rate > torrentialRainRate){
//                println("Torrential rain: $rate, Date: $date, Amount: ${precipAmount}mm, Duration: ${sum.first} minutes")
                res += 1
            }
        }
        return res
    }

    /**
     * Starts looking through duration list from given index
     * Sum up the amount of minutes it has rain until
     * the first time the index is pointing at a date
     * that is not equal to the given date
     */
    fun sumPrecipitationDuration(date: String, duration: List<ClimateObs>, index: Int): Pair<Double, Int> {
        var sumDuration: Double = 0.0
        var lastIndex: Int = 0

        for (i in index until duration.size) {
            lastIndex = i
            if (duration[i].referenceTime.substring(0, 10) != date) {
                break
            }
            sumDuration += duration[i].observations[0].value
        }

        return Pair(sumDuration, lastIndex)
    }
}