package ch.epfl.reminday.format.calendar

import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MyGregorianCalendarTest {

    companion object {
        const val FEBRUARY = 2
        const val APRIL = 4
        const val OCTOBER = 10

        const val LEAP_YEAR = 2000
        const val NON_LEAP_YEAR = 2001
    }

    private lateinit var calendar: MyGregorianCalendar

    @Before
    fun init() {
        calendar = MyGregorianCalendar()
    }

    @Test
    fun isLeapYearWorksOnKnownYears() {
        // check only from ~1800 on
        assertFalse(MyGregorianCalendar.isLeapYear(2003))
        assertTrue(MyGregorianCalendar.isLeapYear(2012))
        assertFalse(MyGregorianCalendar.isLeapYear(1900))
        assertTrue(MyGregorianCalendar.isLeapYear(2000))
    }

    @Test
    fun supportsNullFields() {
        calendar.setNullable(Field.YEAR, null)
        calendar.setNullable(Field.MONTH, null)
        calendar.setNullable(Field.DAY_OF_MONTH, null)

        assertFalse(calendar.isSet(Field.YEAR))
        assertFalse(calendar.isSet(Field.MONTH))
        assertFalse(calendar.isSet(Field.DAY_OF_MONTH))

        assertNull(calendar.get(Field.YEAR))
        assertNull(calendar.get(Field.MONTH))
        assertNull(calendar.get(Field.DAY_OF_MONTH))
    }

    @Test
    fun dayIsConstrained() {
        calendar.set(Field.YEAR, LEAP_YEAR)
        calendar.set(Field.MONTH, APRIL)
        calendar.set(Field.DAY_OF_MONTH, 31)

        assertEquals(LEAP_YEAR, calendar.get(Field.YEAR))
        assertEquals(APRIL, calendar.get(Field.MONTH))
        assertEquals(30, calendar.get(Field.DAY_OF_MONTH))
    }

    @Test
    fun dayGetsConstrainedAfterMonthChange() {
        calendar.set(Field.YEAR, NON_LEAP_YEAR)
        calendar.set(Field.MONTH, OCTOBER)
        calendar.set(Field.DAY_OF_MONTH, 31)

        assertEquals(NON_LEAP_YEAR, calendar.get(Field.YEAR))
        assertEquals(OCTOBER, calendar.get(Field.MONTH))
        assertEquals(31, calendar.get(Field.DAY_OF_MONTH))

        calendar.set(Field.MONTH, FEBRUARY)
        assertEquals(NON_LEAP_YEAR, calendar.get(Field.YEAR))
        assertEquals(FEBRUARY, calendar.get(Field.MONTH))
        assertEquals(28, calendar.get(Field.DAY_OF_MONTH))
    }

    @Test
    fun dayIsCorrectlyConstrainedWhenLeapYear() {
        calendar.set(Field.YEAR, NON_LEAP_YEAR)
        calendar.set(Field.MONTH, FEBRUARY)
        calendar.set(Field.DAY_OF_MONTH, 31)

        assertEquals(NON_LEAP_YEAR, calendar.get(Field.YEAR))
        assertEquals(FEBRUARY, calendar.get(Field.MONTH))
        assertEquals(28, calendar.get(Field.DAY_OF_MONTH))

        calendar.set(Field.YEAR, LEAP_YEAR)
        assertEquals(LEAP_YEAR, calendar.get(Field.YEAR))
        assertEquals(FEBRUARY, calendar.get(Field.MONTH))
        assertEquals(29, calendar.get(Field.DAY_OF_MONTH))
    }

    @Test
    fun hasMaximumAndMinimumSupportedDate() {
        val min: LocalDate = calendar.minimumSupportedDate()
        val max: LocalDate = calendar.maximumSupportedDate()
        val now: LocalDate = LocalDate.now()

        assertTrue(min < max)
        assertTrue(min < now)
        assertTrue(now < max)
    }

    @Test
    fun clearMakesCalendarReturnNull() {
        calendar.set(Field.MONTH, 11)
        assertTrue(calendar.isSet(Field.MONTH))
        calendar.clear(Field.MONTH)
        assertNull(calendar.get(Field.MONTH))
        assertFalse(calendar.isSet(Field.MONTH))

        calendar.set(Field.DAY_OF_MONTH, 18)
        assertTrue(calendar.isSet(Field.DAY_OF_MONTH))
        calendar.clear(Field.DAY_OF_MONTH)
        assertNull(calendar.get(Field.DAY_OF_MONTH))
        assertFalse(calendar.isSet(Field.DAY_OF_MONTH))

        calendar.set(Field.YEAR, 2000)
        assertTrue(calendar.isSet(Field.YEAR))
        calendar.clear(Field.YEAR)
        assertNull(calendar.get(Field.YEAR))
        assertFalse(calendar.isSet(Field.YEAR))
    }

    @Test
    fun getMaximumWorks() {
        val now = LocalDate.now()
        assertEquals(now.year, calendar.getMaximum(Field.YEAR))
        assertEquals(12, calendar.getMaximum(Field.MONTH))
        assertEquals(31, calendar.getMaximum(Field.DAY_OF_MONTH))
    }

    @Test
    fun getLeastMaximumWorks() {
        val now = LocalDate.now()
        assertEquals(now.year, calendar.getLeastMaximum(Field.YEAR))
        assertEquals(12, calendar.getLeastMaximum(Field.MONTH))
        assertEquals(28, calendar.getLeastMaximum(Field.DAY_OF_MONTH))
    }
}