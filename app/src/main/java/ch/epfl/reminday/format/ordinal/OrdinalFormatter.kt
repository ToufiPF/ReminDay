package ch.epfl.reminday.format.ordinal

import java.util.*

fun interface OrdinalFormatter {

    companion object {

        fun getInstance(locale: Locale = Locale.getDefault()): OrdinalFormatter =
            when (locale.language) {
                "en" -> KnownOrdinalFormatters.english
                "fr" -> KnownOrdinalFormatters.french
                else -> OrdinalFormatter { "" }
            }
    }

    fun getOrdinalFor(number: Int): String
}