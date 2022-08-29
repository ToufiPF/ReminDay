package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.data.security.PromptUserUnlock
import ch.epfl.reminday.databinding.ActivityStartBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    @Inject
    lateinit var prompt: PromptUserUnlock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueButton.let { button ->
            if (prompt.canAuthenticate()) {
                promptUser()
                button.setOnClickListener { promptUser() }
            } else {
                launchMainActivity()
                button.setOnClickListener { launchMainActivity() }
            }
        }
    }

    private fun promptUser() {
        lifecycleScope.launch {
            if (prompt.authenticate(this@StartActivity))
                launchMainActivity()
        }
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
