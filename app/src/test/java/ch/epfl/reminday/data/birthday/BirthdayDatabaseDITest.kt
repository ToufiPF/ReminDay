package ch.epfl.reminday.data.birthday

import android.content.Context
import androidx.room.RoomDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BirthdayDatabaseDITest {

    private lateinit var context: Context
    private lateinit var db: BirthdayDatabase
    private lateinit var bDayDao: BirthdayDao
    private lateinit var infoDao: AdditionalInformationDao

    @Before
    fun init() {
        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        db = mockk()
        bDayDao = mockk()
        infoDao = mockk()
    }

    private fun mockkBirthdayDatabase(test: suspend () -> Unit) {
        mockkConstructor(RoomDatabase.Builder::class) {
            every { anyConstructed<RoomDatabase.Builder<*>>().build() } returns db
            every { db.birthdayDao() } returns bDayDao
            every { db.additionalInformationDao() } returns infoDao

            runBlocking {
                test.invoke()
            }
        }
    }

    @Test
    fun providesNonNullDB() = mockkBirthdayDatabase {
        assertEquals(db, BirthdayDatabaseDI.provideDb(context))
    }

    @Test
    fun providesNonNullBirthdayDatabase() = mockkBirthdayDatabase {
        assertEquals(bDayDao, BirthdayDatabaseDI.provideBirthdayDao(db))
    }

    @Test
    fun providesNonNullInfoDatabase() = mockkBirthdayDatabase {
        assertEquals(infoDao, BirthdayDatabaseDI.provideAdditionalInformationDao(db))
    }
}
