package ch.epfl.reminday.ui.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.di.BirthdayDatabaseTestDI
import ch.epfl.reminday.testutils.SafeFragmentScenario
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.utils.Mocks
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
    lateinit var fakeDao: BirthdayDao

    @Before
    fun init() {
        hilt.inject() // injects fakeDao into test case
    }

    @After
    fun clearDao() {
        BirthdayDatabaseTestDI.clear(fakeDao)
    }

    private val onRecycler: ViewInteraction get() = onView(withId(R.id.birthday_list_recycler))

    @Test
    fun listDisplaysBirthdays(): Unit = runBlocking {
        val days = Mocks.birthdays(3) { true }
        fakeDao.insertAll(*days)

        SafeFragmentScenario.launchInHiltContainer<BirthdayListFragment> {
            onRecycler.perform(UITestUtils.waitUntilLoadingCompleted())

            for (day in days) {
                val expectedMatcher = allOf(withText(day.personName), withId(R.id.name_view))

                UITestUtils.waitForView(expectedMatcher)
                onView(expectedMatcher).check(matches(isDisplayed()))
            }
        }
    }
}