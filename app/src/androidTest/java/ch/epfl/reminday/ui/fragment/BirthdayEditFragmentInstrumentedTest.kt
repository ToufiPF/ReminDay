package ch.epfl.reminday.ui.fragment

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.SafeFragmentScenario
import ch.epfl.reminday.di.LocaleTestDI
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import java.util.*

@HiltAndroidTest
class BirthdayEditFragmentInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Locale is EN by default (cf. LocaleTestDI)

    @Test
    fun allViewsAreShownWithDayMonthYearLayout() {
        LocaleTestDI.locale = Locale.FRANCE // dd/MM/yyyy

        SafeFragmentScenario.launchInHiltContainer<BirthdayEditFragment> {
            onView(withId(R.id.day_edit_text)).check(matches(isDisplayed()))
            onView(withId(R.id.month_edit_text)).check(matches(isDisplayed()))
            onView(withId(R.id.year_edit_text)).check(matches(isDisplayed()))
            onView(withId(R.id.date_picker_button)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun allViewsAreShownWithMonthDayYearLayout() {
        LocaleTestDI.locale = Locale.ENGLISH // MM/dd/yyyy

        SafeFragmentScenario.launchInHiltContainer<BirthdayEditFragment> {
            onView(withId(R.id.day_edit_text)).check(matches(isDisplayed()))
            onView(withId(R.id.month_edit_text)).check(matches(isDisplayed()))
            onView(withId(R.id.year_edit_text)).check(matches(isDisplayed()))
            onView(withId(R.id.date_picker_button)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun datePickerOpensWithCorrectDate() {
        SafeFragmentScenario.launchInHiltContainer<BirthdayEditFragment> {
            onView(withId(R.id.month_edit_text)).perform(clearText(), typeText("12"))
            onView(withId(R.id.day_edit_text)).perform(clearText(), typeText("10"))
            onView(withId(R.id.year_edit_text)).perform(clearText(), typeText("1999"))

            onView(withId(R.id.date_picker_button)).perform(click())

            onView(withText("Fri, Dec 10")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun datePickerChangesDateInEditFields() {
        SafeFragmentScenario.launchInHiltContainer<BirthdayEditFragment> {
            onView(withId(R.id.date_picker_button)).perform(click())

            onView(withClassName(equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(2020, 10, 1))
            onView(withText("OK")).perform(click())

            onView(withId(R.id.day_edit_text)).check(matches(withText("1")))
            onView(withId(R.id.month_edit_text)).check(matches(withText("10")))
            onView(withId(R.id.year_edit_text)).check(matches(withText("2020")))
        }
    }
}