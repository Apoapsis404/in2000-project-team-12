package no.uio.ifi.in2000.team12.team12project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val lightMode: Boolean,
    val language: String
)

@Entity(tableName = "favorites")
data class FavoriteLocationEntity(
    @PrimaryKey val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)