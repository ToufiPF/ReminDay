package ch.epfl.reminday.di

import android.content.Context
import androidx.room.Room
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.data.birthday.BirthdayDatabase
import ch.epfl.reminday.data.birthday.BirthdayDatabaseDI
import ch.epfl.reminday.utils.Mocks
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Month
import java.time.MonthDay
import java.time.Year
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BirthdayDatabaseDI::class]
)
object TestBirthdayDatabaseDI {

    @Provides
    @Singleton
    fun provideFakeBirthdayDatabase(@ApplicationContext context: Context): BirthdayDatabase =
        Room.inMemoryDatabaseBuilder(context, BirthdayDatabase::class.java).build()

    @Provides
    @Singleton
    fun provideFakeBirthdayDao(db: BirthdayDatabase): BirthdayDao = db.birthdayDao()

    fun fillIn(dao: BirthdayDao) {
        val faker = Mocks.makeFaker()
        // add some predefined birthdays (with potential edge cases)
        dao.insertAll(
            Birthday(
                faker.superSmashBros.unique.fighter(),
                MonthDay.of(Month.FEBRUARY, 29),
                Year.of(2004)
            ),
            Birthday(
                faker.superSmashBros.unique.fighter(),
                MonthDay.of(Month.MARCH, 31),
                Year.of(-100)
            ),
            Birthday(
                faker.superSmashBros.unique.fighter(),
                MonthDay.of(Month.MARCH, 31),
                Year.of(452)
            ),
            Birthday(
                faker.superSmashBros.unique.fighter(),
                MonthDay.of(Month.MARCH, 31),
                year = null
            ),
        )
        // and some random ones:
        dao.insertAll(*Mocks.birthdays(20, yearKnown = { it % 3 != 0 }))
    }

    fun clear(dao: BirthdayDao) {
        dao.getAll().forEach { dao.delete(it) }
    }
}