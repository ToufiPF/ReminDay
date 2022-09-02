package ch.epfl.reminday.data.birthday

import android.content.Context
import androidx.room.RoomDatabase
import ch.epfl.reminday.security.DatabaseKeyManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class BirthdayDatabaseDITest {

    private lateinit var context: Context
    private lateinit var keyMgr: DatabaseKeyManager
    private lateinit var db: BirthdayDatabase
    private lateinit var bDayDao: BirthdayDao
    private lateinit var infoDao: AdditionalInformationDao

    @Before
    fun init() {
        context = mockk(relaxed = true)
        every { context.applicationContext } returns context

        keyMgr = mockk(relaxUnitFun = true)
        db = mockk()
        bDayDao = mockk()
        infoDao = mockk()

        every { db.birthdayDao() } returns bDayDao
        every { db.additionalInformationDao() } returns infoDao
        every { keyMgr.isDatabaseEncryptionSupported() } returns true
        every { keyMgr.newDatabaseKey() } returns ByteArray(256 / 8) { it.toByte() }
        every { keyMgr.loadDatabaseKey() } returns ByteArray(256 / 8) { it.toByte() }
    }

    private fun mockkBirthdayDatabase(test: suspend () -> Unit) {
        mockkConstructor(RoomDatabase.Builder::class) {
            every { anyConstructed<RoomDatabase.Builder<*>>().build() } returns db

            runBlocking {
                test.invoke()
            }
        }
    }

    @Test
    fun providesKeyManager() = mockkBirthdayDatabase {
        assertNotNull(BirthdayDatabaseDI.provideDbKeyManager(context))
    }

    @Test
    fun providesNonNullDB() = mockkBirthdayDatabase {
        assertEquals(db, BirthdayDatabaseDI.provideDb(context, keyMgr))
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
