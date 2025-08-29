package ch.epfl.reminday.format.date

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShortFormat

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LongFormat

@Module
@InstallIn(SingletonComponent::class)
object DateFormatterDI {

    @ShortFormat
    @Provides
    fun provideShortFormatter(locale: Locale): DateFormatter =
        DateFormatter.shortFormatter(locale)

    @LongFormat
    @Provides
    fun provideLongFormatter(locale: Locale): DateFormatter =
        DateFormatter.longFormatter(locale)
}
