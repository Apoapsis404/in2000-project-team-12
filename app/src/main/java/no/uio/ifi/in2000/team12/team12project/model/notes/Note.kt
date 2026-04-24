package no.uio.ifi.in2000.team12.team12project.model.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String
) {
    val title: String
        get() = content.lineSequence().firstOrNull() ?: ""
}