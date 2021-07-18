package ch.epfl.reminday.testdi

import android.content.Context
import androidx.room.Room
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.data.birthday.BirthdayDatabase
import ch.epfl.reminday.data.birthday.BirthdayDatabaseDI
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

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
    fun provideFakeBirthdayDao(db: BirthdayDatabase): BirthdayDao = db.birthdayDao()
}