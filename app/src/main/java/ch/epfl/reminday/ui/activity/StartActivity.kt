package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.R
import ch.epfl.reminday.databinding.ActivityStartBinding
import ch.epfl.reminday.security.PromptUserUnlock
import ch.epfl.reminday.util.constant.PreferenceNames
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
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

        val prefs = getSharedPreferences(GENERAL_PREFERENCES, MODE_PRIVATE)
        val requireUnlock = prefs.getBoolean(getString(R.string.prefs_require_user_unlock), true)
        if (requireUnlock && prompt.canAuthenticate()) {
            promptUser()
            binding.continueButton.setOnClickListener { promptUser() }
        } else {
            launchMainActivity()
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
