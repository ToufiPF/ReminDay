package ch.epfl.reminday.data.security

import android.content.Context
import androidx.biometric.BiometricManager
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
    @ActivityScoped
    fun provideBiometricManager(@ActivityContext context: Context): PromptUserUnlock =
        PromptUserUnlockImpl(context)
}
