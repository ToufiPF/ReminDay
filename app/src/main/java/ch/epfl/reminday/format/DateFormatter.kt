package ch.epfl.reminday.format

import java.time.MonthDay
import java.time.Year

fun interface DateFormatter {

    fun format(monthDay: MonthDay, year: Year?): String
}