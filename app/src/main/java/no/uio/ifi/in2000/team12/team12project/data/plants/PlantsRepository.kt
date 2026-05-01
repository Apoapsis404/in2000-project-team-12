package no.uio.ifi.in2000.team12.team12project.data.plants

import android.content.Context
import android.util.Log
import androidx.navigationevent.NavigationEventDispatcher
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.Plants.Plant
import java.util.PriorityQueue

class PlantsRepository(private val context: Context){

    private val plantList = getPlantData()

    private fun getTextFromResources(resourceId: Int): String{
        return context.resources.openRawResource(resourceId)
            .bufferedReader()
            .use{
                it.readText()
            }
    }

    private fun getPlantData(): List<Plant>{
        val jsonString = getTextFromResources(R.raw.plants_data)
        return Json.decodeFromString(jsonString)
    }

// Returns a list of <quantity> plants with optimal_temp average closest to <targetTemp>
    fun getBestPlants(targetTemp: Int, quantity: Int = 3, allowNonOptimal: Boolean = true): List<Plant>{
        val list = if (!allowNonOptimal){
            plantList.filter {targetTemp >= it.optimal_temp_min && targetTemp <= it.optimal_temp_max}
        } else plantList
        list.filter { targetTemp >= it.temp_min && targetTemp <= it.temp_max }

        val heap = PriorityQueue<Plant>(compareBy { kotlin.math.abs(((it.optimal_temp_min+it.optimal_temp_max)/2)-targetTemp) })
        list.forEach { heap.add(it) }

        val bestList = mutableListOf<Plant>()
        for(i in 0 until quantity){
            if(heap.isNotEmpty()) bestList.add(heap.poll()!!)
        }
        return bestList
    }

    // does not filter out temperatures outside livable range
    fun getBestPlantsPriorityQueue(targetTemp: Int): PriorityQueue<Plant>{
        val pq = PriorityQueue<Plant>(compareBy { kotlin.math.abs(((it.optimal_temp_min+it.optimal_temp_max)/2)-targetTemp) })
        plantList.forEach { pq.add(it) }
        pq.filter { targetTemp >= it.temp_min && targetTemp <= it.temp_max }
        return pq
    }

    fun printPlant(plant: Plant){
        println("${plant.id} (${plant.name}) : " +
                "optimal_temp: (${plant.optimal_temp_min}-${plant.optimal_temp_max})C, " +
                "livable_temp: (${plant.temp_min}-${plant.temp_max})C")
    }
}