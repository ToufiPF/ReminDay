package ch.epfl.reminday.data.birthday

import ch.epfl.reminday.data.birthday.Comparators.monthDayYearNameOrder
import ch.epfl.reminday.data.birthday.Comparators.nameYearMonthDayOrder
import ch.epfl.reminday.data.birthday.Comparators.yearMonthDayNameOrder
import org.junit.Assert
import org.junit.Test
import java.time.MonthDay
import java.time.Year

class ComparatorsTest {

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
        Assert.assertTrue(monthDayYearNameOrder.compare(b1, b2) > 0) // b1 > b2
        Assert.assertTrue(monthDayYearNameOrder.compare(b2, b3) > 0) // b2 > b3
        Assert.assertTrue(monthDayYearNameOrder.compare(b2, b4) > 0) // b2 > b4
        Assert.assertTrue(monthDayYearNameOrder.compare(b3, b4) > 0) // b2 > b4
        Assert.assertTrue(monthDayYearNameOrder.compare(b2, b5) < 0)
        Assert.assertTrue(monthDayYearNameOrder.compare(b4, b5) < 0)
    }

    @Test
    fun yearMonthDayNameOrderWorks() {
        Assert.assertTrue(yearMonthDayNameOrder.compare(b1, b2) > 0)
        Assert.assertTrue(yearMonthDayNameOrder.compare(b2, b3) > 0)
        Assert.assertTrue(yearMonthDayNameOrder.compare(b3, b4) < 0)
        Assert.assertTrue(yearMonthDayNameOrder.compare(b4, b5) < 0)
    }

    @Test
    fun nameYearMonthDayOrderWorks() {
        Assert.assertTrue(nameYearMonthDayOrder.compare(b1, b2) < 0)
        Assert.assertTrue(nameYearMonthDayOrder.compare(b2, b3) < 0)
        Assert.assertTrue(nameYearMonthDayOrder.compare(b3, b4) < 0)
        Assert.assertTrue(nameYearMonthDayOrder.compare(b4, b5) < 0)
    }
}