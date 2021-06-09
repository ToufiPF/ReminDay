package ch.epfl.reminday.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Birthday::class), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BirthdayDatabase : RoomDatabase() {

    abstract fun birthdayDao(): BirthdayDao
}