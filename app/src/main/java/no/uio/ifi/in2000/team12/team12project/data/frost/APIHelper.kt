package no.uio.ifi.in2000.team12.team12project.data.frost

import no.uio.ifi.in2000.team12.team12project.model.frost.ObsStation
import no.uio.ifi.in2000.team12.team12project.model.frost.ClimateObs
import kotlin.collections.forEach

fun constructTimeSeries(from: String, to: String, repeat: String = "", period: String = ""): String {
    var timeSeries = repeat

    if (timeSeries != "") {
        timeSeries += "/"
    }

    timeSeries += "$from/$to"
    if (period != "") {
        timeSeries += "/$period"
    }

    return timeSeries
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
