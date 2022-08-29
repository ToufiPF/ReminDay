package ch.epfl.reminday.data.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PromptUserUnlockImpl(
    context: Context,
) : PromptUserUnlock {

    private class Callback(
        private val continuation: Continuation<Boolean>
    ) : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Log.e(this::class.java.name, "Authentication error code $errorCode : '$errString'")
            continuation.resume(false)
        }

        override fun onAuthenticationFailed() {
            Log.e(this::class.java.name, "Authentication failed.")
            continuation.resume(false)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            Log.i(this::class.java.name, "Authentication successful")
            continuation.resume(true)
        }
    }

    private val executor = ContextCompat.getMainExecutor(context)
    private val manager = BiometricManager.from(context)
    private val authenticators = DEVICE_CREDENTIAL or BIOMETRIC_WEAK

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setAllowedAuthenticators(authenticators)
        .setConfirmationRequired(false)
        .setTitle("Identification")
        .build()

    override fun canAuthenticate(): Boolean =
        manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

    override suspend fun authenticate(activity: FragmentActivity): Boolean =
        suspendCoroutine { continuation ->
            // Callback implementation will resume the continuation (cf. above)
            val prompt = BiometricPrompt(activity, executor, Callback(continuation))

            prompt.authenticate(promptInfo)
        }
}