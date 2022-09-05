package ch.epfl.reminday.util

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.reminday.EmptyRegularTestActivity
import ch.epfl.reminday.R
import ch.epfl.reminday.util.Extensions.showConfirmationDialog
import ch.epfl.reminday.util.Extensions.showConfirmationDialogWithDoNotAskAgain
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
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

    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private val showDeleteConfirmation: String by lazy {
        context.getString(R.string.prefs_show_delete_confirmation)
    }

    private val onConfirm: ViewInteraction
        get() = onView(withText(R.string.confirm)).inRoot(isDialog())

    private val onCancel: ViewInteraction
        get() = onView(withText(R.string.cancel)).inRoot(isDialog())

    private val onDoNotAskAgain: ViewInteraction
        get() = onView(withText(R.string.do_not_ask_again)).inRoot(isDialog())

    @Before
    fun init() {
        counter.getAndSet(0)

        context = getApplicationContext()
        preferences = context.getSharedPreferences(GENERAL_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
    }

    @After
    fun clean() {
        preferences.edit().clear().commit()
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

        onView(withText(R.string.are_you_sure)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.import_from_contacts_are_you_sure))
            .inRoot(isDialog()).check(matches(isDisplayed()))
        onCancel.check(matches(isDisplayed()))
        onConfirm.check(matches(isDisplayed()))
        onCancel.perform(click()) // must dismiss dialog for test to succeed
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

        onCancel.perform(click())
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

        onConfirm.perform(click())
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
                showDeleteConfirmation,
                incrementCounter
            )
        }

        onView(withText(R.string.are_you_sure)).inRoot(isDialog()).check(matches(isDisplayed()))
        onView(withText(R.string.import_from_contacts_are_you_sure)).inRoot(isDialog())
            .check(matches(isDisplayed()))
        onDoNotAskAgain.check(matches(isDisplayed()))
        onCancel.check(matches(isDisplayed()))
        onConfirm.check(matches(isDisplayed()))
        onCancel.perform(click()) // must dismiss dialog for test to succeed
    }

    @Test
    fun doNotAskAgainDialogSetsPreferenceWhenCheckboxCheckedAndConfirmed() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                showDeleteConfirmation,
                incrementCounter
            )
        }

        onDoNotAskAgain.perform(click())
        onConfirm.perform(click())

        onIdle()

        assertEquals(counter.get(), 1)
        assertFalse(preferences.getBoolean(showDeleteConfirmation, false))
    }

    @Test
    fun doNotAskAgainDialogDoesNotSetPreferenceWhenCheckboxCheckedAndCancelled() {
        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                showDeleteConfirmation,
                incrementCounter
            )
        }

        onDoNotAskAgain.perform(click())
        onCancel.perform(click())

        onIdle()

        assertEquals(counter.get(), 0)
        assertTrue(preferences.getBoolean(showDeleteConfirmation, true))
    }

    @Test
    fun doNotAskAgainDialogDirectlyRunsCallableWhenPreferenceIsSet() {
        preferences.edit()
            .putBoolean(showDeleteConfirmation, false)
            .commit()

        scenarioRule.scenario.onActivity {
            it.showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.import_from_contacts_are_you_sure,
                preferences,
                showDeleteConfirmation,
                incrementCounter
            )
        }

        onIdle()

        onView(withText(R.string.are_you_sure)).check(doesNotExist())

        assertEquals(counter.get(), 1)
    }
}
