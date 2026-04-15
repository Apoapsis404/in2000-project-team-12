package no.uio.ifi.in2000.team12.team12project.data.plants

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.team12.team12project.R
import no.uio.ifi.in2000.team12.team12project.model.Plants.Plant

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


    fun getBestPlants(temp: Int, quantity: Int = 3, allowNonOptimal: Boolean = true): List<Plant>{
        val optimalList = mutableListOf<Plant>()
        plantList.forEach {
            if (temp >= it.optimal_temp_min && temp <= it.optimal_temp_max) {
                optimalList.add(it)
            }
        }
        return optimalList
        //return listOf<Plant>(plantList[0])
    }

    fun printPlant(plant: Plant){
        println("${plant.id} Best plant: (${plant.name}) : " +
                "optimal_temp: (${plant.optimal_temp_min}-${plant.optimal_temp_max})C, " +
                "livable_temp: (${plant.temp_min}-${plant.temp_max})C")
    }
}