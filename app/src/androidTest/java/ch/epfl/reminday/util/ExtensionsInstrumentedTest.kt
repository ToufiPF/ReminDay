package ch.epfl.reminday.util

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.reminday.EmptyRegularTestActivity
import ch.epfl.reminday.R
import ch.epfl.reminday.util.Extensions.showConfirmationDialog
import ch.epfl.reminday.util.Extensions.showConfirmationDialogWithDoNotAskAgain
import ch.epfl.reminday.util.constant.PreferenceNames
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.GeneralPreferenceNames.SKIP_DELETE_CONFIRMATION
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ExtensionsInstrumentedTest {

    private val counter = AtomicInteger(0)
    private val incrementCounter: () -> Unit = { counter.getAndIncrement() }

    @get:Rule
    val scenarioRule = ActivityScenarioRule(EmptyRegularTestActivity::class.java)

    private lateinit var preferences: SharedPreferences

    @Before
    fun init() {
        counter.getAndSet(0)

        val context = getApplicationContext<Context>()
        preferences = context.getSharedPreferences(GENERAL_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit()
            .remove(SKIP_DELETE_CONFIRMATION)
            .commit()
    }

    @After
    fun clean() {
        preferences.edit()
            .remove(SKIP_DELETE_CONFIRMATION)
            .commit()
    }

    @Test
    fun confirmDialogDisplaysTitleMessageAndButtons() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialog(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                incrementCounter
            )
        }

        onView(withText(R.string.are_you_sure)).check(matches(isDisplayed()))
        onView(withText(R.string.import_from_contacts_are_you_sure)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).check(matches(isDisplayed()))
        onView(withText(R.string.confirm)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).perform(click()) // must dismiss dialog for test to succeed
    }

    @Test
    fun confirmDialogDoesNothingOnCancel() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialog(
                R.string.are_you_sure,
                R.string.are_you_sure,
                incrementCounter
            )
        }

        onView(withText(R.string.cancel)).perform(click())
        onIdle()

        assertEquals(counter.get(), 0)
    }

    @Test
    fun confirmDialogRunsCallableOnConfirm() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialog(
                R.string.are_you_sure,
                R.string.are_you_sure,
                incrementCounter
            )
        }

        onView(withText(R.string.confirm)).perform(click())
        onIdle()

        assertEquals(counter.get(), 1)
    }

    @Test
    fun doNotAskAgainDialogDisplaysTitleMessageCheckboxAndButtons() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                SKIP_DELETE_CONFIRMATION,
                incrementCounter
            )
        }

        onView(withText(R.string.are_you_sure)).check(matches(isDisplayed()))
        onView(withText(R.string.import_from_contacts_are_you_sure)).check(matches(isDisplayed()))
        onView(withText(R.string.do_not_ask_again)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).check(matches(isDisplayed()))
        onView(withText(R.string.confirm)).check(matches(isDisplayed()))
        onView(withText(R.string.cancel)).perform(click()) // must dismiss dialog for test to succeed
    }

    @Test
    fun doNotAskAgainDialogSetsPreferenceWhenCheckboxCheckedAndConfirmed() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                SKIP_DELETE_CONFIRMATION,
                incrementCounter
            )
        }

        onView(withText(R.string.do_not_ask_again)).perform(click())
        onView(withText(R.string.confirm)).perform(click())

        onIdle()

        assertEquals(counter.get(), 1)
        assertTrue(preferences.getBoolean(SKIP_DELETE_CONFIRMATION, false))
    }

    @Test
    fun doNotAskAgainDialogDoesNotSetPreferenceWhenCheckboxCheckedAndCancelled() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                SKIP_DELETE_CONFIRMATION,
                incrementCounter
            )
        }

        onView(withText(R.string.do_not_ask_again)).perform(click())
        onView(withText(R.string.cancel)).perform(click())

        onIdle()

        assertEquals(counter.get(), 0)
        assertFalse(preferences.getBoolean(SKIP_DELETE_CONFIRMATION, false))
    }

    @Test
    fun doNotAskAgainDialogDirectlyRunsCallableWhenPreferenceIsSet() {
        preferences.edit()
            .putBoolean(SKIP_DELETE_CONFIRMATION, true)
            .commit()

        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                SKIP_DELETE_CONFIRMATION,
                incrementCounter
            )
        }

        onIdle()

        onView(withText(R.string.are_you_sure)).check(doesNotExist())

        assertEquals(counter.get(), 1)
    }
}
