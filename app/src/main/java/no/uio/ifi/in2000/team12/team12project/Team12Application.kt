package no.uio.ifi.in2000.team12.team12project

import android.app.Application
import no.uio.ifi.in2000.team12.team12project.data.frost.ClimateDataRepository
import no.uio.ifi.in2000.team12.team12project.data.local.AppDatabase
import no.uio.ifi.in2000.team12.team12project.data.plants.PlantsRepository

class Team12Application : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val climateDataRepository by lazy { ClimateDataRepository() }
    val plantsRepository by lazy { PlantsRepository(this) }
}