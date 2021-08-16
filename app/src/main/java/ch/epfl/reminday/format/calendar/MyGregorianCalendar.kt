package ch.epfl.reminday.format.calendar

import java.time.LocalDate
import java.util.*

/**
 * A mutable, non-thread safe class to manipulate dates.
 * Similar to [Calendar] but modified to correspond to the needs of the app.
 * (eg. overflows are simply restricted instead of being rolled unto the next field).
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class MyGregorianCalendar : Cloneable {

    companion object {
        private const val JANUARY = 1
        private const val FEBRUARY = 2
        private const val MARCH = 3
        private const val APRIL = 4
        private const val MAY = 5
        private const val JUNE = 6
        private const val JULY = 7
        private const val AUGUST = 8
        private const val SEPTEMBER = 9
        private const val OCTOBER = 10
        private const val NOVEMBER = 11
        private const val DECEMBER = 12

        // max number of days in a month (in a non-leap year)
        private val MONTH_LENGTH = mapOf(
            JANUARY to 31,
            FEBRUARY to 28,
            MARCH to 31,
            APRIL to 30,
            MAY to 31,
            JUNE to 30,
            JULY to 31,
            AUGUST to 31,
            SEPTEMBER to 30,
            OCTOBER to 31,
            NOVEMBER to 30,
            DECEMBER to 31,
        )

        // below 1650 we use the Julian calendar, it's a pain so we don't support year < 1800
        private const val MIN_YEAR = 1880


        /**
         * Returns true when the given [year] is a leap year.
         */
        fun isLeapYear(year: Int): Boolean = when {
            // year not divisible by 4 -> not leap year
            year % 4 != 0 -> false
            // year divisible by 4 but not 100 -> leap year
            year % 100 != 0 -> true
            // year divisible by 4 and 100, it's a leap year if it's divisible by 400 as well
            else -> year % 400 == 0
        }
    }

    enum class Field {
        /**
         * The field representing the day of the month
         */
        DAY_OF_MONTH,

        /**
         * The field representing the month of the date
         */
        MONTH,

        /**
         * The field representing the year
         */
        YEAR,
    }

    private var fields: EnumMap<Field, Int?> = EnumMap(Field::class.java)

    private val currentYear = LocalDate.now().year

    /**
     * Returns the current value for [field]. Null if not set, otherwise,
     * the value is constrained and guaranteed to be a valid date by the Gregorian calendar.
     */
    fun get(field: Field): Int? = fields[field]?.coerceIn(getActualValidRange(field))

    /**
     * Sets the value of [field] to [value].
     * @param field [Field] the field to set
     * @param value [Int] the value of the field. It may be invalid,
     * in which case it will be constrained before the next call to [get].
     */
    fun set(field: Field, value: Int) {
        // do not coerce the date when setting to be able
        //to move from 28 Feb. to 31 Mar. for instance
        fields[field] = value
    }

    /**
     * Resets the value of [field].
     * After this function is called, [isSet] will return false for the field
     * @param field the [Field] to clear.
     */
    fun clear(field: Field) {
        fields[field] = null
    }

    /**
     * Sets the [field] or reset it depending on whether [value] is null or not.
     * @param field the [Field] to set or reset
     * @param value [Int?] the nullable value to write
     * @see set
     * @see clear
     */
    fun setNullable(field: Field, value: Int?) {
        fields[field] = value
    }

    /**
     * Returns whether the [field] is currently set in the calendar.
     * @param field the [Field] to check
     */
    fun isSet(field: Field): Boolean = fields[field] != null

    /**
     * Returns the lowest value the [field] can possibly ever take.
     * Eg. for [Field.MONTH], the value is 1 (January).
     *
     * Note that the year is limited to be larger than 1800 for simplification purposes.
     */
    fun getMinimum(field: Field): Int = when (field) {
        Field.YEAR -> MIN_YEAR
        // day of month/year, month => min is 1
        else -> 1
    }

    /**
     * Returns the maximum value for [field].
     * Eg. for [Field.DAY_OF_MONTH], the value is 31.
     *
     * Note that [Field.YEAR] is limited to the current year (at runtime).
     */
    fun getMaximum(field: Field): Int = when (field) {
        Field.YEAR -> currentYear
        Field.MONTH -> DECEMBER
        Field.DAY_OF_MONTH -> 31
    }

    /**
     * Returns the lowest maximum value for [field].
     * Eg. for [Field.DAY_OF_MONTH], the value is 28
     *
     * Note that [Field.YEAR] is limited to the current year (at runtime).
     */
    fun getLeastMaximum(field: Field): Int = when (field) {
        Field.YEAR -> currentYear
        Field.MONTH -> DECEMBER
        Field.DAY_OF_MONTH -> MONTH_LENGTH[FEBRUARY]!!
    }

    /**
     * Returns the highest value the [field] can take with respect to the other fields.
     * Eg. for [Field.DAY_OF_MONTH], it will return 28 if the field [Field.MONTH] is set to February
     * and [Field.YEAR] is not a leap year.
     *
     * Note that [Field.YEAR] is limited to the current year (at runtime).
     * Also note that the year is assumed to be a leap year if not set,
     * and the month is assumed to be January if not set.
     */
    fun getActualMaximum(field: Field): Int {
        val year = fields[Field.YEAR]
        val leapYear = year == null || isLeapYear(year)
        return when (field) {
            Field.YEAR -> currentYear
            Field.MONTH -> DECEMBER
            Field.DAY_OF_MONTH -> {
                val month = get(Field.MONTH) ?: JANUARY
                val monthMax = MONTH_LENGTH[month]!!
                if (month == FEBRUARY && leapYear) monthMax + 1 else monthMax
            }
        }
    }

    /**
     * Returns the actual valid range for [field], given the value of the other fields.
     */
    fun getActualValidRange(field: Field): IntRange =
        getMinimum(field)..getActualMaximum(field)

    /**
     * The minimum date the calendar supports.
     */
    fun minimumSupportedDate(): LocalDate = LocalDate.of(MIN_YEAR, 1, 1)

    /**
     * The maximum date the calendar supports.
     */
    fun maximumSupportedDate(): LocalDate = LocalDate.of(LocalDate.now().year, 12, 31)
}
