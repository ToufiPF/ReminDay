package ch.epfl.reminday.data.birthday

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BirthdayDatabaseDI {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): BirthdayDatabase =
        Room.databaseBuilder(context, BirthdayDatabase::class.java, BirthdayDatabase.NAME)
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4)
            .build()

    @Provides
    @Singleton
    fun provideBirthdayDao(db: BirthdayDatabase): BirthdayDao =
        db.birthdayDao()

    @Provides
    @Singleton
    fun provideAdditionalInformationDao(db: BirthdayDatabase): AdditionalInformationDao =
        db.additionalInformationDao()
}
