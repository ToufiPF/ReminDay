package ch.epfl.reminday.di

import ch.epfl.reminday.format.LocaleDI
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.testing.TestInstallIn
import java.util.*

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [LocaleDI::class]
)
object LocaleTestDI {

    private val DEFAULT_LOCALE: Locale = Locale.ENGLISH

    /**
     * Set this to override the default locale [Locale.ENGLISH]
     */
    var locale: Locale? = null

    @Provides
    @ActivityScoped
    fun provideLocale(): Locale {
        val returned = locale ?: DEFAULT_LOCALE
        locale = null
        return returned
    }
}