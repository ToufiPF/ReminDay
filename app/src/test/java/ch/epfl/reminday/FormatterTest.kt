package ch.epfl.reminday

import ch.epfl.reminday.format.DateFormatters
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Month
import java.time.MonthDay
import java.time.Year

class FormatterTest {

    @Test
    fun frenchDateIsCorrectlyFormatted() {
        val f = DateFormatters.FRENCH_LONG
        assertEquals(
            "12 janvier 1987",
            f.format(MonthDay.of(Month.JANUARY, 12), Year.of(1987))
        )
        assertEquals(
            "29 février 2004",
            f.format(MonthDay.of(Month.FEBRUARY, 29), Year.of(2004))
        )
    }
}