package no.uio.ifi.in2000.team12.team12project.model.frost

import android.content.Context
import java.util.Locale

enum class Months(val value: Int, val english : String, val norwegian : String) {
    JANUARY(1, "January", "Januar"),
    FEBRUARY(2, "February", "February"),
    MARCH(3, "March", "Mars"),
    APRIL(4, "April", "April"),
    MAY(5, "May", "Mai"),
    JUNE(6, "June", "Juni"),
    JULY(7, "July", "Juli"),
    AUGUST(8, "August", "August"),
    SEPTEMBER(9, "September", "September"),
    OCTOBER(10, "October", "Oktober"),
    NOVEMBER(11, "November", "November"),
    DECEMBER(12, "December", "Desember")
}

    fun Months.localizedName(context: Context): String {
        return if (Locale.getDefault().language == "nb") norwegian else english
    }
