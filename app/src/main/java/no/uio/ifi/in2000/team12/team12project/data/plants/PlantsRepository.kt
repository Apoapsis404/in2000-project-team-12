package no.uio.ifi.in2000.team12.team12project.data.plants

import android.content.Context
import androidx.core.os.LocaleListCompat
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.plants.Plant
import java.util.PriorityQueue

class PlantsRepository(private val context: Context){

    private val plantListEn: List<Plant> by lazy { getPlantData(R.raw.plants_datav2) }
    private val plantListNb: List<Plant> by lazy { getPlantData(R.raw.plants_datav2_norwegian) }
    private fun getTextFromResources(resourceId: Int): String{
        return context.resources.openRawResource(resourceId)
            .bufferedReader()
            .use{
                it.readText()
            }
    }

    // Checks which language the user is using, and returns the correct data
    private fun getPlantData(resourceId: Int): List<Plant>{
        val jsonString = getTextFromResources(resourceId = resourceId)
        return Json.decodeFromString(jsonString)
    }

    // Returns a priority queue of plants with optimal_temp average closest to <targetTemp>,
    // then humidity average closest to <targetHumidity>, then sunshine average closest to <targetSunshine>,
    // then precipitation average closest to <targetPrecipitation> (if it is not null)
     fun getBestPlantsPriorityQueue(
        targetTemp: Float,
        targetHumidity: Float,
        targetSunshine: Float,
        targetPrecipitation: Float?
    ): PriorityQueue<Plant> {
         //choose the right language data
        val currentLanguage = LocaleListCompat.getAdjustedDefault()[0]?.language ?: "en"
        val plantList = if (currentLanguage == "en") plantListEn else plantListNb

        // sort by optimal_temp, then humidity, then sunshine, then precipitation
        var comparator = compareBy<Plant> { kotlin.math.abs(((it.optimal_temp_min + it.optimal_temp_max) / 2.0) - targetTemp) }
            .thenBy { kotlin.math.abs(((it.humidity_min + it.humidity_max) / 2.0) - targetHumidity) }
            .thenBy { kotlin.math.abs(((it.sunshine_min + it.sunshine_max) / 2.0) - targetSunshine) }

        if (targetPrecipitation != null) {
            comparator = comparator.thenBy { kotlin.math.abs(((it.precipitation_min + it.precipitation_max) / 2.0) - targetPrecipitation) }
        }
        val pq = PriorityQueue(comparator)
        //filter out plants that cannot survive
        plantList
            .filter { targetTemp >= it.temp_min && targetTemp <= it.temp_max }
            .forEach { pq.add(it) }
        return pq
    }

}