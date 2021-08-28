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
        private const val SOME_LEAP_YEAR = 1904

        private fun patternWithYear(style: FormatStyle, locale: Locale): String {
            val pattern: String = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                style, null, IsoChronology.INSTANCE, locale
            )
            // replace any one or more 'y' by 'yyyy'
            //eg. "mm/dd/yy" -> "mm/dd/yyyy"
            return pattern
                .replace("\\b[yY]{1,4}\\b".toRegex(), "yyyy")
                .replace("\\b[uU]{1,4}\\b".toRegex(), "uuuu")
        }

        private fun patternWithoutYear(style: FormatStyle, locale: Locale): String {
            val pattern: String = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                style, null, IsoChronology.INSTANCE, locale
            )
            // remove any non letter char followed by (1 or more) 'y' followed by any non letter char
            // eg. "mm/dd/yy" -> "mm/dd"
            return pattern.replace("\\P{L}*[yYuU]+\\P{L}*".toRegex(), "")
        }
    }

    private val patternWithYear = patternWithYear(style, locale)
    private val patternWithoutYear = patternWithoutYear(style, locale)
    private val yearF = DateTimeFormatter.ofPattern(patternWithYear, locale)
    private val monthF = DateTimeFormatter.ofPattern(patternWithoutYear, locale)

    override fun format(monthDay: MonthDay, year: Year?): String =
        if (year != null)
            yearF.format(LocalDate.of(year.value, monthDay.month, monthDay.dayOfMonth))
        else
            monthF.format(LocalDate.of(SOME_LEAP_YEAR, monthDay.month, monthDay.dayOfMonth))

    override fun pattern(withYear: Boolean): String =
        if (withYear) patternWithYear else patternWithoutYear
}