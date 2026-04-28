package no.uio.ifi.in2000.team12.team12project.data.frost

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

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


fun isFromMonth(referencetime: String, month: Int): Boolean {
    return referencetime.split("-")[1].toInt() == month
}

fun isFromYear(referencetime: String, year: Int): Boolean {
    return referencetime.split("-")[0].toInt() == year
}


fun getStationIdsFromList(stationList: List<ObsStation>): String {
    var stationIds: String = ""
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
