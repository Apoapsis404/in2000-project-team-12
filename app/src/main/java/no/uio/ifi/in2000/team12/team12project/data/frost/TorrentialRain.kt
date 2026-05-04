package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateType

class TorrentialRain(
    private val climateDataRepository: ClimateDataRepository = ClimateDataRepository()
) {

    /*
     * Torrential Rain is calculated as follows:
     * Amount precipitation in a day / sum of minutes it has rained that day
     * sum of minutes is found by adding up the duration in minutes individual hours of the day (annoying!)
     */
    private val torrentialRainRate = 7.5 // mm/hr
    private val minPrecipitationDuration = 30 // minutes

    suspend fun getPrecipitationDuration(sourceId: String, monthsBack: UInt): List<ClimateObs> {
        val elementId = "sum(duration_of_precipitation PT1H)"
        val obs = climateDataRepository.getClimateData(sourceId, elementId,constructTimeSeries(monthsBack))
        return obs
    }

    suspend fun getPrecipitationAmount(sourceId: String, monthsBack: UInt): List<ClimateObs> {
        val elementId = "sum(precipitation_amount P1D)"
        val obs = climateDataRepository.getClimateData(sourceId, elementId,constructTimeSeries(monthsBack))
        return obs
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
                println("Torrential rain: $rate, Date: $date, Amount: ${precipAmount}mm, Duration: ${sum.first} minutes")
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