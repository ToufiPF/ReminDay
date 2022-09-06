package ch.epfl.reminday.ui.view

import android.content.Context
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.SafeFragmentScenario
import ch.epfl.reminday.util.constant.PreferenceNames
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TimePickerPreferenceInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @AndroidEntryPoint
    class TestFragment(
        private val factory: (Context) -> TimePickerPreference
    ) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(ResourcesCompat.ID_NULL, null)
            preferenceManager.sharedPreferencesName = PreferenceNames.GENERAL_PREFERENCES
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            preferenceScreen = PreferenceScreen(context!!, null)
            super.onCreate(savedInstanceState)
        }

        override fun onResume() {
            super.onResume()

            val pref = factory.invoke(context!!)
            preferenceScreen.addPreference(pref)
        }
    }

    private fun runTest(test: () -> Unit) {
        val factory = { context: Context ->
            TimePickerPreference(context)
        }

        SafeFragmentScenario.launchInHiltContainer(instantiate = { TestFragment(factory) }) {
            test.invoke()
        }
    }

    @Test
    fun preferenceDisplaysAllFields(): Unit = runTest {
        onView(withId(R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.value)).check(matches(isDisplayed()))
        onView(withId(R.id.summary)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingPreferenceDisplaysTimePickerDialog(): Unit = runTest {
        onView(withId(R.id.title)).perform(click())

        onView(withText(R.string.prefs_notification_time_title)).inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}