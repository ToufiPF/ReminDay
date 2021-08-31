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

    val isYearKnown: Boolean get() = year != null
}