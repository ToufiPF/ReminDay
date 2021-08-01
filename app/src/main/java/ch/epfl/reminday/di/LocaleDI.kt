package ch.epfl.reminday.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import java.util.*

@Module
@InstallIn(ActivityComponent::class)
object LocaleDI {

    @Provides
    @ActivityScoped
    fun provideLocale(): Locale = Locale.getDefault()
}