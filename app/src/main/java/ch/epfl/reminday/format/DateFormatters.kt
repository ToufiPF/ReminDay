package ch.epfl.reminday.format

import java.util.*

object DateFormatters {

    val AUTO_SHORT: DateFormatter
        get() = LongDateFormatter(Locale.getDefault())
    val AUTO_LONG: DateFormatter
        get() = LongDateFormatter(Locale.getDefault())

    val FRENCH_SHORT: DateFormatter = LongDateFormatter(Locale.FRENCH)
    val FRENCH_LONG: DateFormatter = LongDateFormatter(Locale.FRENCH)

    val ENGLISH_SHORT: DateFormatter = LongDateFormatter(Locale.ENGLISH)
    val ENGLISH_LONG: DateFormatter = LongDateFormatter(Locale.ENGLISH)

}