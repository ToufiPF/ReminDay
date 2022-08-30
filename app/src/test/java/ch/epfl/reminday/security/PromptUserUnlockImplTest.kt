package ch.epfl.reminday.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricViewModel
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.Executor

class PromptUserUnlockImplTest {

    private lateinit var activity: FragmentActivity
    private lateinit var executor: Executor
    private lateinit var manager: BiometricManager
    private lateinit var viewModel: BiometricViewModel

    private lateinit var callbacks: LinkedList<AuthenticationCallback>
    private lateinit var promptUserUnlock: PromptUserUnlock

    private fun runTest(test: suspend () -> Unit): Unit = runBlocking {
        mockkStatic(ContextCompat::class, BiometricManager::class) {
            every { ContextCompat.getMainExecutor(any()) } returns executor
            every { BiometricManager.from(any()) } returns manager

            mockkConstructor(
                BiometricPrompt::class,
                BiometricPrompt.PromptInfo.Builder::class,
                ViewModelProvider::class
            ) {
                every {
                    anyConstructed<BiometricPrompt.PromptInfo.Builder>().build()
                } returns mockk(relaxed = true)

                every {
                    anyConstructed<ViewModelProvider>()[any<Class<ViewModel>>()]
                } returns viewModel
                every {
                    anyConstructed<ViewModelProvider>()[any<String>(), any<Class<ViewModel>>()]
                } returns viewModel

                test()
            }
        }
    }

    @Before
    fun init() {
        activity = mockk()
        executor = mockk()
        manager = mockk()
        viewModel = mockk(relaxed = true)
        callbacks = LinkedList<AuthenticationCallback>()

        every { activity.applicationContext } returns activity
        every { activity.baseContext } returns activity
        every { activity.mainExecutor } returns executor
        every { activity.supportFragmentManager } returns mockk(relaxed = true)
        every { activity.viewModelStore } returns mockk(relaxed = true)
        every { activity.defaultViewModelProviderFactory } returns mockk(relaxed = true)
        every { activity.defaultViewModelCreationExtras } returns mockk(relaxed = true)

        every { manager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS

        // package-private method, accessible with reflection through the mockk ;
        // capture the callback here (not possible to do in BiometricPrompt's constructor)
        every { viewModel["setClientCallback"](capture(callbacks)) } returns Unit
    }

    @Test
    fun canAuthenticateUsesBiometricManager(): Unit = runTest {
        promptUserUnlock = PromptUserUnlockImpl(activity)

        every { manager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS
        assertTrue(promptUserUnlock.canAuthenticate())

        every { manager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
        assertFalse(promptUserUnlock.canAuthenticate())

        every { manager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
        assertFalse(promptUserUnlock.canAuthenticate())
    }

    @Test
    fun authenticateReturnsTrueOnCallbackSuccess(): Unit = runTest {
        every { anyConstructed<BiometricPrompt>().authenticate(any()) }.coAnswers {
            delay(100)
            callbacks.last.onAuthenticationSucceeded(mockk(relaxed = true))
        }
        promptUserUnlock = PromptUserUnlockImpl(activity)

        assertTrue(promptUserUnlock.authenticate(activity))
    }

    @Test
    fun authenticateReturnsFalseOnCallbackFailure(): Unit = runTest {
        every { anyConstructed<BiometricPrompt>().authenticate(any()) }.coAnswers {
            delay(100)
            callbacks.last.onAuthenticationFailed()
        }

        promptUserUnlock = PromptUserUnlockImpl(activity)
        assertFalse(promptUserUnlock.authenticate(activity))
    }

    @Test
    fun authenticateReturnsFalseOnCallbackError(): Unit = runTest {
        every { anyConstructed<BiometricPrompt>().authenticate(any()) }.coAnswers {
            delay(100)
            callbacks.last.onAuthenticationError(
                BiometricPrompt.ERROR_NO_BIOMETRICS, "No hardware supports biometrics (mocked)"
            )
        }

        promptUserUnlock = PromptUserUnlockImpl(activity)
        assertFalse(promptUserUnlock.authenticate(activity))
    }
}
