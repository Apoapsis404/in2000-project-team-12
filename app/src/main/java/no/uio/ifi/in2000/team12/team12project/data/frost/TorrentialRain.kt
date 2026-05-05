package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.data.exceptions.NoAvailableTimeSeriesException
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import kotlin.math.min

class TorrentialRain(
    private val climateDataRepository: ClimateDataRepository = ClimateDataRepository()
) {
    companion object {
        private const val DURATION_ELEMENT = "sum(duration_of_precipitation PT1H)"
        private const val AMOUNT_ELEMENT = "sum(precipitation_amount P1D)"

        /** mm/hr (source: https://www.baranidesign.com/faq-articles/2020/1/19/rain-rate-intensity-classification) */
        private const val TORRENTIAL_RAIN_RATE_THRESHOLD = 7.5

        /** Minimum instances per year to be considered a threat */
        private const val INSTANCES_PER_YEAR_THRESHOLD = 5

        /** Minimum duration (minutes) to avoid noise from low-duration observations */
        private const val MIN_PRECIPITATION_DURATION_MINUTES = 30

        private const val MAX_MONTHS_BACK = 120u
        private const val DATE_STRING_LENGTH = 10
    }

    /**
     * Checks if the station has a history of torrential rain that exceeds the threshold.
     */
    suspend fun isTorrentialRainThreat(sourceId: String): Boolean {
        val (durationStart, amountStart) = try {
            val dStart = climateDataRepository.getClimateTimeSeries(sourceId, DURATION_ELEMENT).take(DATE_STRING_LENGTH)
            val aStart = climateDataRepository.getClimateTimeSeries(sourceId, AMOUNT_ELEMENT).take(DATE_STRING_LENGTH)
            dStart to aStart
        } catch (e: NoAvailableTimeSeriesException) {
            return false
        }

        val months = min(min(monthsSince(durationStart), monthsSince(amountStart)).toUInt(), MAX_MONTHS_BACK)

        val durationData = getClimateData(sourceId, months, DURATION_ELEMENT)
        val amountData = getClimateData(sourceId, months, AMOUNT_ELEMENT)

        val totalInstances = calculateTorrentialRainInstances(durationData, amountData)
        val yearsCount = if (months >= 12u) (months / 12u).toInt() else 1
        val averageInstancesPerYear = totalInstances / yearsCount

        return averageInstancesPerYear >= INSTANCES_PER_YEAR_THRESHOLD
    }

    suspend fun getClimateData(sourceId: String, monthsBack: UInt, elementId: String): List<ClimateObs> {
        return climateDataRepository.getClimateData(sourceId, elementId, constructTimeSeries(monthsBack))
    }

    /**
     * Calculates total instances of torrential rain by comparing daily precipitation amount
     * against total rain duration for that day.
     */
    fun calculateTorrentialRainInstances(durationObs: List<ClimateObs>, amountObs: List<ClimateObs>): Int {
        // Group hourly duration observations by date for efficient lookup
        val durationByDate = durationObs
            .groupBy { it.referenceTime.take(DATE_STRING_LENGTH) }
            .mapValues { (_, obsList) ->
                obsList.sumOf { it.observations.firstOrNull()?.value ?: 0.0 }
            }

        return amountObs.count { dailyAmount ->
            val date = dailyAmount.referenceTime.take(DATE_STRING_LENGTH)
            val precipAmount = findMaxObs(dailyAmount)
            val totalDurationMinutes = durationByDate[date] ?: 0.0

            if (totalDurationMinutes < MIN_PRECIPITATION_DURATION_MINUTES || precipAmount <= 0.0) {
                false
            } else {
                val rateMmPerHour = precipAmount / (totalDurationMinutes / 60.0)
                rateMmPerHour > TORRENTIAL_RAIN_RATE_THRESHOLD
            }
        }
    }
}
