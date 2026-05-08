package no.uio.ifi.in2000.team12.team12project.data.frost

import com.google.android.gms.maps.model.LatLng
import no.uio.ifi.in2000.team12.team12project.model.ClimateChartData
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import no.uio.ifi.in2000.team12.team12project.model.frost.Months
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun constructTimeSeries(n: UInt, currentDate: String = getCurrentDate()): String {
    val start = getStartOfDate(currentDate)
    return "${lastNMonths(n, start)}/$start"
}


/*
 * Gets current date as string for time series
 * Because of the need to target API 24+
 * Have to use old version of getting date
 */
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun getStartOfDate(date: String): String {
    val year = date.split("-")[0]
    val month = date.split("-")[1]
    return "$year-$month-01"
}



/*
 * Returns TimeSeries for last N months from beginning of current month
 * N can be more than a year
 * Example: Last 5 months from 2023-03-01 -> 2022-10-01
 */
fun lastNMonths(n: UInt, currentDate: String): String {
    val currentMonth = currentDate.split("-")[1].toInt()
    var fromMonth: Int = (currentMonth - n.toInt()).mod(12)
    if (fromMonth == 0) {
        fromMonth = 12
    }

    var years: Int = (n / 12u).toInt()
    if (currentMonth - fromMonth < 0) {
        years += 1
    }

    val fromYear = currentDate.split("-")[0].toInt() - years
    //println("fromYear: $fromYear, fromMonth: $fromMonth")
    return "$fromYear-${fromMonth.toString().padStart(2, '0')}-01"
}

fun monthsSince(date: String, currentDate: String = getCurrentDate()): Int {

//    println("Date: $date, Current Date: $currentDate")

    val currentYear = currentDate.split("-")[0].toInt()
    val currentMonth = currentDate.split("-")[1].toInt()

    val year = date.split("-")[0].toInt()
    val month = date.split("-")[1].toInt()

//    println("Year: $year, Month: $month")

    return (currentYear - year) * 12 + (currentMonth - month)
}

fun yearsSince(date: String, currentDate: String = getCurrentDate()): Int {
    val currentYear = currentDate.split("-")[0].toInt()
    val year = date.split("-")[0].toInt()
    return currentYear - year
}


fun isFromMonth(referencetime: String, month: Int): Boolean {
    return referencetime.split("-")[1].toInt() == month
}

fun isFromYear(referencetime: String, year: Int): Boolean {
    return referencetime.split("-")[0].toInt() == year
}

/*
 * Rounds LatLng to n decimals
 */
fun LatLng.Round(decimals: Int = 2): LatLng {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val lat = (latitude * multiplier).toInt() / multiplier
    val lng = (longitude * multiplier).toInt() / multiplier
    return LatLng(lat, lng)
}

fun getStationIdsFromList(stationList: List<ObsStation>): String {
    var stationIds = ""
    for (station in stationList) {
        if (stationIds != ""){
            stationIds += ","
        }
        stationIds += station.id
    }
    return stationIds
}

fun printClimateObsList(climateObsList: List<ClimateObs>) {
    if (climateObsList.isEmpty()) {
        println("No climate observations found.")
        return
    }

    climateObsList.forEach { obs ->
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
    val years = mutableListOf<Double>()
    val values = mutableListOf<Double>()

    climateObsList.forEach { obs ->
        val year = extractYear(obs.referenceTime)
        if (year != null) {
            years += year.toDouble()
            values += findMeanObs(obs)
        }
    }

    return ClimateChartData(
        years = years,
        values = values,
    )
}


fun getObsFromYear(year: Int, climateObsList: List<ClimateObs>): List<ClimateObs> {
    if (climateObsList.isEmpty()) return emptyList()

    return climateObsList.filter { obs ->
        extractYear(obs.referenceTime) == year
    }
}

/**
 * Returns observations from the internally cached list whose referenceTime is in the given month.
 * Used by the integration tests after getWeatherDataFailing has populated weatherObsList.
 */
fun getObsFromMonth(month: Months, climateObsList: List<ClimateObs>): List<ClimateObs> {
    if (climateObsList.isEmpty()) return emptyList()

    return climateObsList.filter { obs ->
        extractMonth(obs.referenceTime) == month.value
    }
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
 * Tests assume the list is non-empty, so we keep the non-null Double return type.
 */
fun findMeanValue(climateObsList: List<ClimateObs>): Double {
    require(climateObsList.isNotEmpty()) {
        "Cannot compute mean value of an empty list"
    }

    // Mean of each WeatherObs
    val obsMeans = climateObsList.map { obs ->
        findMeanObs(obs)
    }

    // Mean of those means
    return obsMeans.average()
}

/**
 * Compute the mean value of a single WeatherObs.
 * Tests assume observations is non-empty.
 */
fun findMeanObs(climateObs: ClimateObs): Double {
    val observations = climateObs.observations
    require(observations.isNotEmpty()) {
        "Cannot compute mean of WeatherObs with no observations"
    }

    val sum = observations.sumOf { it.value }
    return sum / observations.size
}

fun findMaxObs(climateObs: ClimateObs): Double {
    val observations = climateObs.observations
    require(observations.isNotEmpty()) {
        "Cannot compute max of ClimateObs with no observations"
    }
    return observations.maxOf { it.value }
}
