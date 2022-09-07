package ch.epfl.reminday.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfl.reminday.R
import ch.epfl.reminday.util.constant.ArgumentNames.PREFERENCES_ID
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PreferencesActivityInstrumentedTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private fun launchPreferencesActivity(
        pref_name: String?,
        test: (ActivityScenario<PreferencesActivity>) -> Unit
    ) {
        val intent = Intent(getApplicationContext(), PreferencesActivity::class.java)
        intent.putExtra(PREFERENCES_ID, pref_name)

        ActivityScenario.launch<PreferencesActivity>(intent).use(test)
    }

    @Test
    fun directlyFinishesWhenInvalidPreferencesName(): Unit = launchPreferencesActivity(null) {
        assertEquals(Activity.RESULT_CANCELED, it.result.resultCode)
    }

    @Test
    fun displaysAllGeneralOptions(): Unit = launchPreferencesActivity(GENERAL_PREFERENCES) {
        onView(withText(R.string.prefs_show_delete_confirmation_title))
            .check(matches(isDisplayed()))

        onView(withText(R.string.prefs_require_user_unlock_title))
            .check(matches(isDisplayed()))
    }
}