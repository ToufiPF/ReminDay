package ch.epfl.reminday.data.birthday

import androidx.room.TypeConverter
import java.time.MonthDay
import java.time.Year

object Converters {

    @TypeConverter
    fun monthDayToShort(monthDay: MonthDay): Short {
        return ((monthDay.monthValue shl 8) or monthDay.dayOfMonth).toShort()
    }

    @TypeConverter
    fun shortToMonthDay(short: Short): MonthDay {
        val month = short.toInt() ushr 8
        val day = short.toInt() and 0xFF
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