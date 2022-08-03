package ch.epfl.reminday.data.birthday

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Birthday::class, AdditionalInformation::class],
    exportSchema = true,
    version = 4,
)
@TypeConverters(Converters::class)
abstract class BirthdayDatabase : RoomDatabase() {

    companion object {
        const val NAME = "birthdays_db"
    }

    abstract fun birthdayDao(): BirthdayDao

    abstract fun additionalInformationDao(): AdditionalInformationDao
}