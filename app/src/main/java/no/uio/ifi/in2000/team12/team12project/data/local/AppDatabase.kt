package no.uio.ifi.in2000.team12.team12project.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import no.uio.ifi.in2000.team12.team12project.data.local.dao.FavoriteDao
import no.uio.ifi.in2000.team12.team12project.data.local.dao.NotesDao
import no.uio.ifi.in2000.team12.team12project.data.local.dao.SettingsDao
import no.uio.ifi.in2000.team12.team12project.model.notes.Note

@Database(entities = [SettingsEntity::class, FavoriteLocationEntity::class, Note::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingDao(): SettingsDao
    abstract fun favoriteDao(): FavoriteDao

    abstract fun notesDao(): NotesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "team12_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
