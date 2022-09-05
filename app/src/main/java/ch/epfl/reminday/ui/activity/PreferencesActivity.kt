package ch.epfl.reminday.ui.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.preference.PreferenceFragmentCompat
import ch.epfl.reminday.R
import ch.epfl.reminday.ui.activity.utils.BackArrowActivity
import ch.epfl.reminday.ui.fragment.preferences.GeneralPreferencesFragment
import ch.epfl.reminday.util.constant.ArgumentNames.PREFERENCES_ID
import ch.epfl.reminday.util.constant.PreferenceNames

/**
 * Activity used to display the [PreferenceFragmentCompat] corresponding to the passed [PREFERENCES_ID].
 */
class PreferencesActivity : BackArrowActivity(R.layout.activity_preferences) {

    private val onBackPressed = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // TODO: show confirmation dialog if changes occurred
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment = when (intent.getStringExtra(PREFERENCES_ID)) {
            PreferenceNames.GENERAL_PREFERENCES -> GeneralPreferencesFragment()
            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .add(R.id.preferences_container, fragment)
                .commit()
        } ?: finish()

        onBackPressedDispatcher.addCallback(onBackPressed)
    }
}
