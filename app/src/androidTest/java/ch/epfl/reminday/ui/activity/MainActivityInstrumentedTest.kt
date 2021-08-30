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
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.utils.Mocks
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MainActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val scenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var dao: BirthdayDao

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun clear() {
        Intents.release()
    }

    @Test
    fun addBirthdayButtonLaunchesAddBirthdayActivity() {
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.add_birthday_item_text)).perform(click())

        intended(hasComponent(BirthdayEditActivity::class.java.name))
    }

    @Test
    fun birthdayListIsDisplayed(): Unit = runBlocking {
        dao.insertAll(Mocks.birthday(yearKnown = false))

        onView(withId(R.id.birthday_list_recycler))
            .perform(UITestUtils.waitUntilLoadingCompleted())
            .check(matches(isDisplayed()))
    }
}