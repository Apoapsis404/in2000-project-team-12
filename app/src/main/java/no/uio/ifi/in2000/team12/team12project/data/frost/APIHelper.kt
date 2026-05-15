package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.model.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DATE_FORMAT = "yyyy-MM-dd"

/**
 * Gets current date as string for time series.
 * Targeting API 24+, using SimpleDateFormat.
 */
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    return sdf.format(Date())
}

fun constructTimeSeries(n: UInt, currentDate: String = getCurrentDate()): String {
    val start = getStartOfDate(currentDate)
    return "${lastNMonths(n, start)}/$start"
}

fun getStartOfDate(date: String): String {
    val year = extractYear(date) ?: 0
    val month = extractMonth(date) ?: 1
    return "%04d-%02d-01".format(year, month)
}

/**
 * Returns TimeSeries for last N months from beginning of current month.
 * Example: Last 5 months from 2023-03-01 -> 2022-10-01.
 */
fun lastNMonths(n: UInt, currentDate: String): String {
    val calendar = Calendar.getInstance().apply {
        time = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(currentDate) ?: Date()
        add(Calendar.MONTH, -(n.toInt()))
    }
    return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar.time)
}

fun monthsSince(date: String, currentDate: String = getCurrentDate()): Int {
    val dates = date.split(",")
    val startDate = dates[0]
    if (startDate.isEmpty()) return 0
    
    val endDate = if (dates.size > 1 && dates[1].isNotEmpty()) dates[1] else currentDate

    val endYear = extractYear(endDate) ?: return 0
    val endMonth = extractMonth(endDate) ?: 0
    val startYear = extractYear(startDate) ?: return 0
    val startMonth = extractMonth(startDate) ?: 0

    return (endYear - startYear) * 12 + (endMonth - startMonth)
}

fun yearsSince(date: String, currentDate: String = getCurrentDate()): Int {
    val currentYear = extractYear(currentDate) ?: 0
    val year = extractYear(date) ?: 0
    return currentYear - year
}

fun isFromMonth(referenceTime: String, month: Int): Boolean {
    return extractMonth(referenceTime) == month
}

fun isFromYear(referenceTime: String, year: Int): Boolean {
    return extractYear(referenceTime) == year
}



fun getStationIdsFromList(stationList: List<ObsStation>): String {
    return stationList.joinToString(",") { it.id }
}

fun printClimateObsList(climateObsList: List<ClimateObs>) {
    if (climateObsList.isEmpty()) {
        println("No climate observations found.")
        return
    }

    val maxElement = 5

    climateObsList.subList(0, maxElement).forEach { obs ->
        println("-------------------------------------------")
        println("Source ID: ${obs.sourceId}")
        println("Time: ${obs.referenceTime}")
        println("Observations:")

        obs.observations.forEach { detail ->
            println("  - ${detail.elementId}: ${detail.value} ${detail.unit}")
        }
    }
    println("-------------------------------------------")
}

fun getYearAndData(climateObsList: List<ClimateObs>): ClimateChartData {
    val (years, values) = climateObsList.mapNotNull { obs ->
        if (obs.observations.isEmpty()) return@mapNotNull null
        extractYear(obs.referenceTime)?.let { year ->
            year.toDouble() to findMeanObs(obs)
        }
    }.unzip()

    return ClimateChartData(years = years, values = values)
}

fun getObsFromYear(year: Int, climateObsList: List<ClimateObs>): List<ClimateObs> {
    return climateObsList.filter { extractYear(it.referenceTime) == year }
}

/**
 * Returns observations from the internally cached list whose referenceTime is in the given month.
 */
fun getObsFromMonth(month: Months, climateObsList: List<ClimateObs>): List<ClimateObs> {
    return climateObsList.filter { extractMonth(it.referenceTime) == month.value }
}

/**
 * Extract year from referenceTime, assuming format like "YYYY-MM-DD..." (e.g. "2023-01-01").
 * Returns null if parsing fails.
 */
fun extractYear(referenceTime: String): Int? =
    referenceTime.takeIf { it.length >= 4 }
        ?.substring(0, 4)
        ?.toIntOrNull()

/**
 * Extract month (1-12) from referenceTime, assuming format like "YYYY-MM-DD..." (e.g. "2023-01-01").
 * Returns null if parsing fails.
 */
fun extractMonth(referenceTime: String): Int? =
    referenceTime.takeIf { it.length >= 7 }
        ?.substring(5, 7)
        ?.toIntOrNull()

/**
 * Compute the mean of the per-observation means.
 */
fun findMeanValue(climateObsList: List<ClimateObs>): Double {
    require(climateObsList.isNotEmpty()) { "Cannot compute mean value of an empty list" }
    return climateObsList.map { findMeanObs(it) }.average()
}

/**
 * Compute the mean value of a single ClimateObs.
 */
fun findMeanObs(climateObs: ClimateObs): Double {
    require(climateObs.observations.isNotEmpty()) { "Cannot compute mean of ClimateObs with no observations" }
    return climateObs.observations.map { it.value }.average()
}

fun findMaxObs(climateObs: ClimateObs): Double {
    require(climateObs.observations.isNotEmpty()) { "Cannot compute max of ClimateObs with no observations" }
    return climateObs.observations.maxOf { it.value }
}
