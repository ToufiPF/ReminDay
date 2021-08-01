package ch.epfl.reminday.di

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

    @Provides
    @ActivityScoped
    fun provideLocale(): Locale = Locale.ENGLISH
}