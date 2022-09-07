package ch.epfl.reminday.ui.view

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.SafeFragmentScenario
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
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
            preferenceScreen = preferenceManager.createPreferenceScreen(context!!)

            val pref = factory.invoke(requireContext())
            assertTrue(preferenceScreen.addPreference(pref))
        }
    }

    private fun runTest(test: () -> Unit) {
        val factory = { context: Context ->
            TimePickerPreference(context).apply {
                isPersistent = false
                isSelectable = true
                isVisible = true
                shouldDisableView = false
                key = "test"
                setSummary(R.string.prefs_notification_time_summary)
                setTitle(R.string.prefs_notification_time_title)
            }
        }

        SafeFragmentScenario.launchInHiltContainer(instantiate = { TestFragment(factory) }) {
            test.invoke()
        }
    }

    @Test
    fun preferenceDisplaysAllFields(): Unit = runTest {
        onView(withId(android.R.id.title)).check(matches(isDisplayed()))
        onView(withId(R.id.value)).check(matches(isDisplayed()))
        onView(withId(android.R.id.summary)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingPreferenceDisplaysTimePickerDialog(): Unit = runTest {
        onView(withId(android.R.id.title)).perform(click())

        onView(withText(R.string.prefs_notification_time_title)).inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}
