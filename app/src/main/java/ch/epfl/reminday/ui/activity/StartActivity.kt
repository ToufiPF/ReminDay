package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import ch.epfl.reminday.databinding.ActivityStartBinding
import ch.epfl.reminday.util.constant.ArgumentNames
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    private val authenticators = DEVICE_CREDENTIAL or BIOMETRIC_WEAK or BIOMETRIC_STRONG
    private lateinit var executor: Executor
    private lateinit var manager: BiometricManager

    private lateinit var prompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executor = ContextCompat.getMainExecutor(this)
        manager = BiometricManager.from(this)

        promptInfo = PromptInfo.Builder()
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false)
            .setTitle("Identification")
            .build()
        prompt = BiometricPrompt(this, executor, object : AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e(this::class.java.name, "Authentication error occurred: $errString")
            }

            override fun onAuthenticationFailed() {
                Log.e(this::class.java.name, "Authentication failed.")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.i(this::class.java.name, "Authentication successful")
                launchMainActivity()
            }
        })

        binding.continueButton.let { button ->
            if (manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
                prompt.authenticate(promptInfo)
                button.setOnClickListener { prompt.authenticate(promptInfo) }
            } else {
                launchMainActivity()
                button.setOnClickListener { launchMainActivity() }
            }
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
