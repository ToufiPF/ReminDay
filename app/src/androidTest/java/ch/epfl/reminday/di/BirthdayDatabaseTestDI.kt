package ch.epfl.reminday.di

import android.content.Context
import androidx.room.Room
import ch.epfl.reminday.data.birthday.*
import ch.epfl.reminday.util.Mocks
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.Month
import java.time.MonthDay
import java.time.Year
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BirthdayDatabaseDI::class]
)
object BirthdayDatabaseTestDI {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): BirthdayDatabase =
        Room.inMemoryDatabaseBuilder(context, BirthdayDatabase::class.java).build()

    @Provides
    @Singleton
    fun provideBirthdayDao(db: BirthdayDatabase): BirthdayDao =
        db.birthdayDao()

    @Provides
    @Singleton
    fun provideAdditionalInformationDao(db: BirthdayDatabase): AdditionalInformationDao =
        db.additionalInformationDao()

    fun fillIn(dao: BirthdayDao): Unit = runBlocking(Dispatchers.IO) {
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
                Year.of(1958)
            ),
            Birthday(
                faker.superSmashBros.unique.fighter(),
                MonthDay.of(Month.MAY, 19),
                Year.of(2000)
            ),
            Birthday(
                faker.superSmashBros.unique.fighter(),
                MonthDay.of(Month.APRIL, 11),
                year = null
            ),
        )
        // and some random ones:
        dao.insertAll(*Mocks.birthdays(20, yearKnown = { it % 3 != 0 }))
    }

    fun clear(dao: BirthdayDao): Unit = runBlocking(Dispatchers.IO) {
        dao.getAll().forEach { dao.delete(it) }
    }

    fun clear(dao: AdditionalInformationDao): Unit = runBlocking(Dispatchers.IO) {
        dao.getAll().forEach { dao.delete(it) }
    }
}