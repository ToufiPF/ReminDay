package ch.epfl.reminday.ui.fragment.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import ch.epfl.reminday.R
import ch.epfl.reminday.util.constant.PreferenceNames

class GeneralPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PreferenceNames.GENERAL_PREFERENCES
        setPreferencesFromResource(R.xml.preferences_general, null)
    }
}
