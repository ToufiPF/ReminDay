package ch.epfl.reminday.format

import java.time.MonthDay
import java.time.Year
import java.time.format.FormatStyle
import java.util.*

fun interface DateFormatter {

    companion object {
        val SHORT_AUTO: DateFormatter
            get() = SimpleFormatter(FormatStyle.SHORT, Locale.getDefault())
        val LONG_AUTO: DateFormatter
            get() = SimpleFormatter(FormatStyle.LONG, Locale.getDefault())
    }

    fun format(monthDay: MonthDay, year: Year?): String
}