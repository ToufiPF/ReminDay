package ch.epfl.reminday.ui.activity

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.UITestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val scenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun clear() {
        Intents.release()
    }

    @Test
    fun addBirthdayButtonLaunchesAddBirthdayActivity() {
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.title_add_birthday)).perform(click())

        intended(hasComponent(AddBirthdayActivity::class.java.name))
    }

    @Test
    fun birthdayListIsDisplayed() {
        onView(withId(R.id.birthday_list_recycler))
            .perform(UITestUtils.waitUntilLoadingCompleted())
            .check(matches(isDisplayed()))
    }
}