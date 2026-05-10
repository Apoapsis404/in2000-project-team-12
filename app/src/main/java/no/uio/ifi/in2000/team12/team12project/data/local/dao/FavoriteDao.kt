package no.uio.ifi.in2000.team12.team12project.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team12.team12project.data.local.FavoriteLocationEntity

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteLocationEntity>>

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteLocationEntity)

    @Query("DELETE FROM favorites WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("UPDATE favorites SET name = :newName WHERE name = :oldName")
    suspend fun changeFavoriteLocationName(oldName: String, newName: String)
}