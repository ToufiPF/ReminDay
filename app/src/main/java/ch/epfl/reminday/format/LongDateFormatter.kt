package ch.epfl.reminday.format

import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.*

class LongDateFormatter(
    locale: Locale
) : DateFormatter {

    private val yearFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy LLL dd", locale)
    private val monthFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("LLL dd", locale)

    override fun format(monthDay: MonthDay, year: Year?): String {
        return if (year != null)
            this.yearFormat.format(LocalDate.of(year.value, monthDay.month, monthDay.dayOfMonth))
        else monthFormat.format(LocalDate.of(1804, monthDay.month, monthDay.dayOfMonth))
    }
}