package ch.epfl.reminday.format

import ch.epfl.reminday.format.date.SimpleDateFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Month.*
import java.time.MonthDay
import java.time.Year
import java.time.format.FormatStyle
import java.util.*

class FormatterTest {

    @Test
    fun shortFrenchDate() {
        val f = SimpleDateFormatter(FormatStyle.SHORT, Locale.FRENCH)
        assertEquals(
            "01/11/1904",
            f.format(MonthDay.of(NOVEMBER, 1), Year.of(1904))
        )
        assertEquals(
            "08/10",
            f.format(MonthDay.of(OCTOBER, 8), null)
        )
        assertEquals(
            "29/02",
            f.format(MonthDay.of(FEBRUARY, 29), null)
        )
    }

    @Test
    fun shortEnglishDate() {
        val f = SimpleDateFormatter(FormatStyle.SHORT, Locale.ENGLISH)
        assertEquals(
            "12/1/1904",
            f.format(MonthDay.of(DECEMBER, 1), Year.of(1904))
        )
        assertEquals(
            "12/1/2004",
            f.format(MonthDay.of(DECEMBER, 1), Year.of(2004))
        )
        assertEquals(
            "12/1",
            f.format(MonthDay.of(DECEMBER, 1), null)
        )
        assertEquals(
            "2/29",
            f.format(MonthDay.of(FEBRUARY, 29), null)
        )
    }

    @Test
    fun longFrenchDate() {
        val f = SimpleDateFormatter(FormatStyle.LONG, Locale.FRENCH)
        assertEquals(
            "12 janvier 1987",
            f.format(MonthDay.of(JANUARY, 12), Year.of(1987))
        )
        assertEquals(
            "29 février 2004",
            f.format(MonthDay.of(FEBRUARY, 29), Year.of(2004))
        )
        assertEquals(
            "12 avril",
            f.format(MonthDay.of(APRIL, 12), null)
        )
        assertEquals(
            "29 février",
            f.format(MonthDay.of(FEBRUARY, 29), null)
        )
    }

    @Test
    fun longEnglishDate() {
        val f = SimpleDateFormatter(FormatStyle.LONG, Locale.ENGLISH)
        assertEquals(
            "December 1, 1904",
            f.format(MonthDay.of(DECEMBER, 1), Year.of(1904))
        )
        assertEquals(
            "March 16",
            f.format(MonthDay.of(MARCH, 16), null)
        )
        assertEquals(
            "February 29",
            f.format(MonthDay.of(FEBRUARY, 29), null)
        )
    }
}