package ch.epfl.reminday.data.birthday;

import android.content.Context
import androidx.room.RoomDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class BirthdayDatabaseDITest {

    private lateinit var context: Context
    private lateinit var db: BirthdayDatabase
    private lateinit var dao: BirthdayDao

    @Before
    fun init() {
        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        db = mockk()
        dao = mockk()
    }

    private fun mockkBirthdayDatabase(test: suspend () -> Unit) {
        mockkConstructor(RoomDatabase.Builder::class) {
            every { anyConstructed<RoomDatabase.Builder<*>>().build() } returns db
            every { db.birthdayDao() } returns dao

            runBlocking {
                test.invoke()
            }
        }
    }

    @Test
    fun providesNonNullBirthdayDatabase() = mockkBirthdayDatabase {
        assertEquals(dao, BirthdayDatabaseDI.provideBirthdayDao(context))
    }
}
