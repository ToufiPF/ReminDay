package ch.epfl.reminday.format.date

import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

internal class SimpleDateFormatter(style: FormatStyle, locale: Locale) : DateFormatter {

    @Suppress("SpellCheckingInspection")
    companion object {
        private const val LEAP_YEAR = 1904

        private fun makeYearFormatter(style: FormatStyle, locale: Locale): DateTimeFormatter {
            var pattern: String = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                style, null, IsoChronology.INSTANCE, locale
            )
            // replace any one or more 'y' by 'yyyy'
            //eg. "mm/dd/yy" -> "mm/dd/yyyy"
            pattern = pattern.replace("\\b[yY]{1,4}\\b".toRegex(), "yyyy")
            pattern = pattern.replace("\\b[uU]{1,4}\\b".toRegex(), "uuuu")
            return DateTimeFormatter.ofPattern(pattern, locale)
        }

        private fun makeMonthFormatter(style: FormatStyle, locale: Locale): DateTimeFormatter {
            var pattern: String = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                style, null, IsoChronology.INSTANCE, locale
            )
            // remove any non letter char followed by (1 or more) 'y' followed by any non letter char
            // eg. "mm/dd/yy" -> "mm/dd"
            pattern = pattern.replace("\\P{L}*[yYuU]+\\P{L}*".toRegex(), "")
            return DateTimeFormatter.ofPattern(pattern, locale)
        }
    }

    private val yearF = makeYearFormatter(style, locale)
    private val monthF = makeMonthFormatter(style, locale)

    override fun format(monthDay: MonthDay, year: Year?): String {
        return if (year != null)
            yearF.format(LocalDate.of(year.value, monthDay.month, monthDay.dayOfMonth))
        else
            monthF.format(LocalDate.of(LEAP_YEAR, monthDay.month, monthDay.dayOfMonth))
    }
}