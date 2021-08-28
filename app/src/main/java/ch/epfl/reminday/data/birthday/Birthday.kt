package ch.epfl.reminday.data.birthday

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.MonthDay
import java.time.Year

/**
 * An entity representing a birthday in the DB.
 * Parcelable so it can be passed as argument to an activity/fragment.
 */
@Parcelize
@Entity(indices = [Index(value = arrayOf("monthDay")), Index(value = arrayOf("year", "monthDay"))])
data class Birthday(
    @PrimaryKey val personName: String,
    val monthDay: MonthDay,
    val year: Year? = null,
) : Parcelable {

    companion object Comparators {
        val monthDayYearNameOrder: Comparator<Birthday> = Comparator { b1, b2 ->
            val dayCmp = b1.monthDay.compareTo(b2.monthDay)
            if (dayCmp != 0) return@Comparator dayCmp

            // ignore year field if one of them is null
            if (b1.year != null && b2.year != null) {
                val yearCmp = b1.year.value.compareTo(b2.year.value)
                if (yearCmp != 0) return@Comparator yearCmp
            }

            return@Comparator b1.personName.compareTo(b2.personName)
        }

        val yearMonthDayNameOrder: Comparator<Birthday> = Comparator { b1, b2 ->
            // ignore year field if one of them is null
            if (b1.year != null && b2.year != null) {
                val yearCmp = b1.year.value.compareTo(b2.year.value)
                if (yearCmp != 0) return@Comparator yearCmp
            }

            val dayCmp = b1.monthDay.compareTo(b2.monthDay)
            if (dayCmp != 0) return@Comparator dayCmp

            return@Comparator b1.personName.compareTo(b2.personName)
        }

        val nameYearMonthDayOrder: Comparator<Birthday> = Comparator { b1, b2 ->
            val nameCmp = b1.personName.compareTo(b2.personName)
            if (nameCmp != 0) return@Comparator nameCmp

            // ignore year field if one of them is null
            if (b1.year != null && b2.year != null) {
                val yearCmp = b1.year.value.compareTo(b2.year.value)
                if (yearCmp != 0) return@Comparator yearCmp
            }

            return@Comparator b1.monthDay.compareTo(b2.monthDay)
        }
    }

    val isYearKnown: Boolean get() = year != null
}