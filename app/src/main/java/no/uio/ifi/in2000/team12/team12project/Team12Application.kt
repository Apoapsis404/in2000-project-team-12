package no.uio.ifi.in2000.team12.team12project

import android.app.Application
import no.uio.ifi.in2000.team12.team12project.data.local.AppDatabase

class Team12Application : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}