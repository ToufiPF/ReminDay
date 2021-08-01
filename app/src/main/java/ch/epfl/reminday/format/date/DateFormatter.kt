package ch.epfl.reminday.format.date

import java.time.MonthDay
import java.time.Year
import java.time.format.FormatStyle
import java.util.*

fun interface DateFormatter {

    companion object {
        fun shortFormatter(locale: Locale = Locale.getDefault()): DateFormatter =
            SimpleDateFormatter(FormatStyle.SHORT, locale)

        fun longFormatter(locale: Locale = Locale.getDefault()): DateFormatter =
            SimpleDateFormatter(FormatStyle.LONG, locale)
    }

    fun format(monthDay: MonthDay, year: Year?): String
}