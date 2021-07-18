package ch.epfl.reminday.testdi

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
import io.github.serpro69.kfaker.faker
import java.time.Month
import java.time.MonthDay
import java.time.Year
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.random.asJavaRandom

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
    fun provideFakeBirthdayDao(db: BirthdayDatabase): BirthdayDao {
        val rng = Random(2021L)
        val faker = faker {
            config {
                random = rng.asJavaRandom()
                uniqueGeneratorRetryLimit = 500
            }
        }

        val dao = db.birthdayDao()
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
        dao.insertAll(*Mocks.birthdays(10, false))
        dao.insertAll(*Mocks.birthdays(10, true))

        return dao
    }
}