package ch.epfl.reminday.data.birthday

import android.content.Context
import androidx.room.Room
import ch.epfl.reminday.security.DatabaseKeyManager
import ch.epfl.reminday.security.DatabaseKeyManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BirthdayDatabaseDI {

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
        fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)

        val key = keyMgr.loadDatabaseKey()
        if (key != null) {
            val supportFactory = SupportFactory(key)
            openHelperFactory(supportFactory)
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
