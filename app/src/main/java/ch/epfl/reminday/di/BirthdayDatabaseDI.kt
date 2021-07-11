package ch.epfl.reminday.di

import android.content.Context
import androidx.room.Room
import ch.epfl.reminday.data.BirthdayDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BirthdayDatabaseDI {

    @Singleton
    @Provides
    fun provideBirthdayDatabase(@ApplicationContext context: Context): BirthdayDatabase {
        return Room.databaseBuilder(context, BirthdayDatabase::class.java, "birthdays").build()
    }
}