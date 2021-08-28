package ch.epfl.reminday.ui.view

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.SafeViewScenario
import ch.epfl.reminday.testutils.NumberPickerTestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DatePickerViewInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val onDay: ViewInteraction get() = onView(withId(R.id.day))
    private val onMonth: ViewInteraction get() = onView(withId(R.id.month))
    private val onYear: ViewInteraction get() = onView(withId(R.id.year))


    private fun factory(yearEnabled: Boolean) = { context: Context ->
        DatePickerView(context).apply { isYearEnabled = yearEnabled }
    }

    @Test
    fun yearIsInvisibleWhenDisabled() {
        SafeViewScenario.launchInHiltContainer(factory(yearEnabled = false)) {
            onDay.check(matches(isDisplayed()))
            onMonth.check(matches(isDisplayed()))
            onYear.check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun yearIsVisibleWhenEnabled() {
        SafeViewScenario.launchInHiltContainer(factory(yearEnabled = true)) {
            onDay.check(matches(isDisplayed()))
            onMonth.check(matches(isDisplayed()))
            onYear.check(matches(isDisplayed()))
        }
    }

    @Test
    fun dayIsConstrainedDependingOnMonthAndYear() {
        SafeViewScenario.launchInHiltContainer(factory(yearEnabled = true)) {
            onDay.perform(NumberPickerTestUtils.setValueByJumping(31))
            onMonth.perform(NumberPickerTestUtils.setValueByJumping(12))
            onYear.perform(NumberPickerTestUtils.setValueByJumping(1960))

            onMonth.perform(NumberPickerTestUtils.setValueByJumping(2))
            onDay.check(matches(NumberPickerTestUtils.withValue(29)))

            onYear.perform(NumberPickerTestUtils.setValueIncrementally(1961))
            onDay.check(matches(NumberPickerTestUtils.withValue(28)))
        }
    }

    @Test
    fun februaryHas29DaysWhenYearIsDisabled() {
        SafeViewScenario.launchInHiltContainer(factory(yearEnabled = true)) { scenario ->
            onDay.perform(NumberPickerTestUtils.setValueByJumping(29))
            onMonth.perform(NumberPickerTestUtils.setValueByJumping(2))
            onYear.perform(NumberPickerTestUtils.setValueByJumping(1961))

            onDay.check(matches(NumberPickerTestUtils.withValue(28)))
            scenario.onView { it.isYearEnabled = false }

            onDay.perform(NumberPickerTestUtils.setValueIncrementally(29))
            onDay.check(matches(NumberPickerTestUtils.withValue(29)))
        }
    }

    @Test
    fun settingFieldsUpdatesThem() {
        SafeViewScenario.launchInHiltContainer(factory(yearEnabled = true)) { scenario ->
            scenario.onView {
                it.year = 2000
                it.month = 2
                it.day = 29
            }

            onDay.check(matches(NumberPickerTestUtils.withValue(29)))
            onMonth.check(matches(NumberPickerTestUtils.withValue(2)))
            onYear.check(matches(NumberPickerTestUtils.withValue(2000)))
        }
    }
}