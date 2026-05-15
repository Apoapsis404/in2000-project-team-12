package no.uio.ifi.in2000.team12.team12project.data.local

import androidx.room.TypeConverter
import no.uio.ifi.in2000.team12.team12project.model.frost.Languages

class Converters {
    @TypeConverter
    fun fromLanguage(languages: Languages): String = languages.name

    @TypeConverter
    fun toLanguage(value: String): Languages = try {
        Languages.valueOf(value)
    } catch (_: IllegalArgumentException) {
        Languages.ENGLISH
    }
}