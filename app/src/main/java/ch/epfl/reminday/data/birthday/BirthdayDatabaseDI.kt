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
    fun provideBirthdayDao(@ApplicationContext context: Context): BirthdayDao {
        val db = Room.databaseBuilder(context, BirthdayDatabase::class.java, BirthdayDatabase.NAME)
            .build()
        return db.birthdayDao()
    }
}