package ch.epfl.reminday.data.contacts

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContactQueryDI {

    @Provides
    @Singleton
    fun provideContactQuery(@ApplicationContext context: Context): ContactQuery =
        ContactQuery(context)
}