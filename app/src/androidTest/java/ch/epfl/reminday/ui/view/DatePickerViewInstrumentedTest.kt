package ch.epfl.reminday.ui.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import ch.epfl.reminday.R
import ch.epfl.reminday.SafeViewScenario
import ch.epfl.reminday.testutils.NumberPickerTestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DatePickerViewInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val onDay: ViewInteraction get() = onView(withId(R.id.day))
    private val onMonth: ViewInteraction get() = onView(withId(R.id.month))
    private val onYear: ViewInteraction get() = onView(withId(R.id.year))

    @Test
    fun dayIsConstrainedDependingOnMonthAndYear() {
        SafeViewScenario.launchInHiltContainer({ context -> DatePickerView(context) }) {
            onDay.perform(NumberPickerTestUtils.setValueByJumping(31))
            onMonth.perform(NumberPickerTestUtils.setValueByJumping(12))
            onYear.perform(NumberPickerTestUtils.setValueByJumping(1960))

            onMonth.perform(NumberPickerTestUtils.setValueByJumping(2))
            onDay.check(matches(NumberPickerTestUtils.withValue(29)))

            onYear.perform(NumberPickerTestUtils.setValueIncrementally(1961))
            onDay.check(matches(NumberPickerTestUtils.withValue(28)))
        }
    }
}