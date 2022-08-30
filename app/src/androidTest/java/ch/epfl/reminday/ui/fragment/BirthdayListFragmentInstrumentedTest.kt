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
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.testutils.EspressoMatchers
import ch.epfl.reminday.testutils.EspressoMatchers.withBackgroundColorRes
import ch.epfl.reminday.testutils.SafeFragmentScenario
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.ui.activity.BirthdaySummaryActivity
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.ArgumentNames
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
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

    // necessary otherwise frag.validateAuthentication() won't compile
    @Suppress("RemoveExplicitTypeArguments")
    private fun launchBirthdayListFragment(
        test: (SafeFragmentScenario<BirthdayListFragment>) -> Unit
    ) = SafeFragmentScenario.launchInHiltContainer<BirthdayListFragment> { scenario ->
        onRecycler.perform(UITestUtils.waitUntilPopulated())

        test.invoke(scenario)
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

    @Test
    fun birthdayOnCurrentDayIsHighlighted(): Unit = runBlocking {
        val faker = Mocks.makeFaker()
        val bDay1 = Birthday(
            faker.freshPriceOfBelAir.unique.characters(),
            MonthDay.now(),
            null
        )
        val bDay2 = Birthday(
            faker.freshPriceOfBelAir.unique.characters(),
            MonthDay.now(),
            Year.of(1999)
        )
        val bDay3 = Birthday(
            faker.freshPriceOfBelAir.unique.characters(),
            MonthDay.now().withDayOfMonth(1 + (LocalDate.now().dayOfMonth + 1) % 28),
            null
        )
        dao.insertAll(bDay1, bDay2, bDay3)

        launchBirthdayListFragment {
            onRecycler.perform(UITestUtils.waitUntilPopulated())

            for (bDay in listOf(bDay1, bDay2)) {
                onView(withText(bDay.personName))
                    .check(matches(isDescendantOfA(withBackgroundColorRes(R.color.corn_silk))))
            }

            onView(withText(bDay3.personName))
                .check(matches(not(isDescendantOfA(withBackgroundColorRes(R.color.corn_silk)))))
        }
    }
}
