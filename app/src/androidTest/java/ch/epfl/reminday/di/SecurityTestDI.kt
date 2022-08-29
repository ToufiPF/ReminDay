package ch.epfl.reminday.di

import ch.epfl.reminday.data.security.PromptUserUnlock
import ch.epfl.reminday.data.security.SecurityDI
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito
import org.mockito.Mockito.`when` as whenever

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [SecurityDI::class]
)
object SecurityTestDI {

    val prompt: PromptUserUnlock = Mockito.mock(PromptUserUnlock::class.java).also {
        whenever(it.canAuthenticate()).thenReturn(false)
    }

    @Provides
    @ActivityScoped
    fun providePromptUserUnlock(): PromptUserUnlock = prompt

    fun reset() {
        Mockito.reset(prompt)
        whenever(prompt.canAuthenticate()).thenReturn(false)
    }
}
