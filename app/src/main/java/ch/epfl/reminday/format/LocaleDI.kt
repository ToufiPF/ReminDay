package ch.epfl.reminday.format

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.*

@Module
@InstallIn(SingletonComponent::class)
object LocaleDI {

    @Provides
    fun provideLocale(): Locale = Locale.getDefault()
}