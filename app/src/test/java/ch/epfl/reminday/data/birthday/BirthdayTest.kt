package ch.epfl.reminday.data.birthday

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.MonthDay
import java.time.Year

class BirthdayTest {

    @Test
    fun isYearKnownReturnsTrueWhenYearKnown() {
        val b = Birthday(
            personName = "A",
            monthDay = MonthDay.of(10, 1),
            year = Year.of(2000)
        )

        assertTrue(b.isYearKnown)
    }

    @Test
    fun isYearKnownReturnsFalseWhenYearNotKnown() {
        val b = Birthday(
            personName = "B",
            monthDay = MonthDay.of(3, 10)
        )
        assertFalse(b.isYearKnown)
    }
}