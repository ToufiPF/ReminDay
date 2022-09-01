package ch.epfl.reminday.security

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object SecurityDI {

    @Provides
    fun provideBiometricManager(@ActivityContext context: Context): PromptUserUnlock =
        PromptUserUnlockImpl(context)
}
