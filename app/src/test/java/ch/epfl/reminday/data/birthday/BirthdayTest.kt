package ch.epfl.reminday.data.birthday

import ch.epfl.reminday.data.birthday.Birthday.Comparators.monthDayYearNameOrder
import ch.epfl.reminday.data.birthday.Birthday.Comparators.nameYearMonthDayOrder
import ch.epfl.reminday.data.birthday.Birthday.Comparators.yearMonthDayNameOrder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.MonthDay
import java.time.Year

class BirthdayTest {

    private val b1 = Birthday(
        personName = "A",
        monthDay = MonthDay.of(10, 1),
        year = Year.of(2000)
    )
    private val b2 = Birthday(
        personName = "B",
        monthDay = MonthDay.of(3, 10),
        year = Year.of(1999)
    )
    private val b3 = Birthday(
        personName = "C",
        monthDay = MonthDay.of(3, 10),
        year = Year.of(1977)
    )
    private val b4 = Birthday(
        personName = "D",
        monthDay = MonthDay.of(3, 9),
        year = Year.of(2000)
    )

    private val b5 = Birthday(
        personName = "E",
        monthDay = MonthDay.of(3, 10)
    )

    @Test
    fun monthDayYearNameOrderWorks() {
        assertTrue(monthDayYearNameOrder.compare(b1, b2) > 0) // b1 > b2
        assertTrue(monthDayYearNameOrder.compare(b2, b3) > 0) // b2 > b3
        assertTrue(monthDayYearNameOrder.compare(b2, b4) > 0) // b2 > b4
        assertTrue(monthDayYearNameOrder.compare(b3, b4) > 0) // b2 > b4
        assertTrue(monthDayYearNameOrder.compare(b2, b5) < 0)
        assertTrue(monthDayYearNameOrder.compare(b4, b5) < 0)
    }

    @Test
    fun yearMonthDayNameOrderWorks() {
        assertTrue(yearMonthDayNameOrder.compare(b1, b2) > 0)
        assertTrue(yearMonthDayNameOrder.compare(b2, b3) > 0)
        assertTrue(yearMonthDayNameOrder.compare(b3, b4) < 0)
        assertTrue(yearMonthDayNameOrder.compare(b4, b5) < 0)
    }

    @Test
    fun nameYearMonthDayOrderWorks() {
        assertTrue(nameYearMonthDayOrder.compare(b1, b2) < 0)
        assertTrue(nameYearMonthDayOrder.compare(b2, b3) < 0)
        assertTrue(nameYearMonthDayOrder.compare(b3, b4) < 0)
        assertTrue(nameYearMonthDayOrder.compare(b4, b5) < 0)
    }

    @Test
    fun isYearKnownWorks() {
        assertTrue(b1.isYearKnown)
        assertFalse(b5.isYearKnown)
    }
}