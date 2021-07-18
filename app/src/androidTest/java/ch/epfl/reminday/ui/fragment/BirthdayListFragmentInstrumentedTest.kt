package ch.epfl.reminday.ui.fragment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.di.TestBirthdayDatabaseDI
import ch.epfl.reminday.launchFragmentInHiltContainer
import ch.epfl.reminday.utils.Mocks
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
        TestBirthdayDatabaseDI.clear(fakeDao)
    }

    @Test
    fun listDisplaysBirthdays() {
        launchFragmentInHiltContainer<BirthdayListFragment>().use {
            val days = Mocks.birthdays(3) { true }
            fakeDao.insertAll(*days)

            onView(allOf(withText(days[0].personName), withId(R.id.birthday_list_item_name_view)))
                .check(matches(allOf(isDisplayed())))
        }
    }
}