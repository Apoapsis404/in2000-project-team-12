package no.uio.ifi.in2000.team12.team12project.model.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Note (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String
) {
    val title: String
        get() {
            val sentenceEndings = charArrayOf('.', '!', '?', ':', ';', '\n')
            val index = content.indexOfAny(sentenceEndings)

            return if (index != -1) {
                // Return everything up to and including the first punctuation mark
                content.substring(0, index + 1).trim()
            } else {
                // If no sentence ending is found, return the whole content (or first line)
                content.lineSequence().firstOrNull()?.trim() ?: ""
            }
        }
}