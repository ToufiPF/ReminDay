package ch.epfl.reminday.ui.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.testutils.SafeFragmentScenario
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.ui.activity.BirthdaySummaryActivity
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.ArgumentNames
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class BirthdayListFragmentInstrumentedTest {

    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @Inject
    lateinit var dao: BirthdayDao

    @Before
    fun init() {
        Intents.init()
        hilt.inject() // injects fakeDao into test case
    }

    @After
    fun clearDao() {
        Intents.release()
    }

    private fun launchBirthdayListFragment(
        test: (SafeFragmentScenario<BirthdayListFragment>) -> Unit
    ) = SafeFragmentScenario.launchInHiltContainer<BirthdayListFragment> {
        onRecycler.perform(UITestUtils.waitUntilPopulated())

        test.invoke(it)
    }

    private val onRecycler: ViewInteraction get() = onView(withId(R.id.birthday_list_recycler))

    @Test
    fun listDisplaysBirthdays(): Unit = runBlocking {
        val days = Mocks.birthdays(3) { true }
        dao.insertAll(*days)

        launchBirthdayListFragment {
            for (day in days) {
                onView(allOf(withId(R.id.name_view), withText(day.personName)))
                    .check(matches(isDisplayed()))
            }
        }
    }

    @Test
    fun clickingOnItemOpensSummaryActivity(): Unit = runBlocking {
        val birthday = Mocks.birthday(yearKnown = true)
        dao.insertAll(birthday)

        launchBirthdayListFragment {
            onRecycler.perform(UITestUtils.waitUntilPopulated())

            onView(withText(birthday.personName)).perform(click())

            intended(
                allOf(
                    hasComponent(BirthdaySummaryActivity::class.java.name),
                    hasExtra(ArgumentNames.BIRTHDAY, birthday),
                )
            )
        }
    }
}