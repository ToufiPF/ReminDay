package ch.epfl.reminday.data.birthday

import android.content.Context
import android.util.Log
import androidx.room.Room
import ch.epfl.reminday.security.DatabaseKeyManager
import ch.epfl.reminday.security.DatabaseKeyManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BirthdayDatabaseDI {

    private const val LOG_TAG = "BirthdayDatabaseDI"

    @Provides
    fun provideDbKeyManager(@ApplicationContext context: Context): DatabaseKeyManager =
        DatabaseKeyManagerImpl(context)

    @Provides
    @Singleton
    fun provideDb(
        @ApplicationContext context: Context,
        keyMgr: DatabaseKeyManager
    ): BirthdayDatabase = Room.databaseBuilder(
        context,
        BirthdayDatabase::class.java,
        BirthdayDatabase.NAME
    ).apply {
        fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5)

        if (keyMgr.isDatabaseEncryptionSupported()) {
            keyMgr.loadDatabaseKey()?.let { key ->
                Log.i(LOG_TAG, "Opening encrypted database.")

                val supportFactory = SupportOpenHelperFactory(key)
                openHelperFactory(supportFactory)
            } ?: Log.i(LOG_TAG, "Opening database in clear (key not set up).")
        } else {
            Log.i(LOG_TAG, "Opening database in clear (API too low).")
        }
    }.build()

    @Provides
    @Singleton
    fun provideBirthdayDao(db: BirthdayDatabase): BirthdayDao =
        db.birthdayDao()

    @Provides
    @Singleton
    fun provideAdditionalInformationDao(db: BirthdayDatabase): AdditionalInformationDao =
        db.additionalInformationDao()
}
