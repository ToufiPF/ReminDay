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

    @Singleton
    @Provides
    fun provideBirthdayDatabase(@ApplicationContext context: Context): BirthdayDatabase =
        Room.databaseBuilder(context, BirthdayDatabase::class.java, BirthdayDatabase.NAME).build()

    @Provides
    @Singleton
    fun provideBirthdayDao(db: BirthdayDatabase): BirthdayDao = db.birthdayDao()
}