package no.uio.ifi.in2000.team12.team12project.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team12.team12project.data.local.SettingsEntity

@Dao
interface SettingsDao {
    //Settings
    @Query("SELECT * FROM settings WHERE id = 0")
    fun getSettings(): Flow<SettingsEntity?>

    @Upsert
    suspend fun saveSettings(settings: SettingsEntity)

}