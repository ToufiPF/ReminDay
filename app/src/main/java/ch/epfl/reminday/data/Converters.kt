package ch.epfl.reminday.data

import androidx.room.TypeConverter
import java.time.MonthDay
import java.time.Year

object Converters {

    @TypeConverter
    fun monthDayToShort(monthDay: MonthDay): Short {
        return ((monthDay.dayOfMonth shl 8) or monthDay.monthValue).toShort()
    }

    @TypeConverter
    fun shortToMonthDay(short: Short): MonthDay {
        val month = short.toInt() and 0xFF
        val day = short.toInt() ushr 8
        return MonthDay.of(month, day)
    }


    @TypeConverter
    fun yearToInt(year: Year?): Int? {
        return year?.value
    }

    @TypeConverter
    fun intToYear(int: Int?): Year? {
        return int?.let { Year.of(it) }
    }
}