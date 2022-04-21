package ch.epfl.reminday.testutils

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.NumberPicker
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.util.HumanReadables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers

object NumberPickerTestUtils {

    private val mIncrement = ViewActions.actionWithAssertions(
        GeneralClickAction(
            Tap.SINGLE,
            GeneralLocation.BOTTOM_CENTER,
            Press.FINGER,
            InputDevice.SOURCE_UNKNOWN,
            MotionEvent.BUTTON_PRIMARY
        )
    )

    private val mDecrement: ViewAction = ViewActions.actionWithAssertions(
        GeneralClickAction(
            Tap.SINGLE,
            GeneralLocation.TOP_CENTER,
            Press.FINGER,
            InputDevice.SOURCE_UNKNOWN,
            MotionEvent.BUTTON_PRIMARY
        )
    )

    /**
     * Returns an action that increments the value of a [NumberPicker]
     */
    fun increment(): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.instanceOf(NumberPicker::class.java)
        override fun getDescription(): String = "incrementing the value of a NumberPicker"
        override fun perform(uiController: UiController?, view: View?) {
            mIncrement.perform(uiController, view)
        }
    }

    /**
     * Returns an action that decrements the value of a [NumberPicker]
     */
    fun decrement(): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.instanceOf(NumberPicker::class.java)
        override fun getDescription(): String = "decrementing the value of a NumberPicker"
        override fun perform(uiController: UiController?, view: View?) {
            mDecrement.perform(uiController, view)
        }
    }

    // checks that view is a NumberPicker & that value is in valid range
    private fun checkPreconditions(view: View?, value: Int, description: String?): NumberPicker {
        val picker = view as? NumberPicker ?: throw PerformException.Builder()
            .withActionDescription(description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(ClassCastException("view is not a NumberPicker"))
            .build()

        if (value !in picker.minValue..picker.maxValue)
            throw PerformException.Builder()
                .withActionDescription(description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(IllegalArgumentException("Value $value is not in the min/max range of the NumberPicker"))
                .build()

        return picker
    }

    /**
     * Returns an action that sets the value of a [NumberPicker] to [value],
     * triggering [NumberPicker.OnValueChangeListener] if any.
     */
    fun setValueByJumping(value: Int): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.instanceOf(NumberPicker::class.java)
        override fun getDescription(): String = "Set number picker value to $value"
        override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadUntilIdle()

            val picker = checkPreconditions(view, value, description)

            // trigger onValueChange callback by manually changing and restoring the value
            when {
                !(picker.minValue <= value && value <= picker.maxValue) ->
                    throw IllegalArgumentException("Invalid value (not ${picker.minValue} <= $value <= ${picker.maxValue})")
                picker.minValue < value -> {
                    picker.value = value - 1
                    mIncrement.perform(uiController, view)
                }
                value < picker.maxValue -> {
                    picker.value = value + 1
                    mDecrement.perform(uiController, view)
                }
                else -> { // min == value == max
                    picker.value = value
                }
            }
            uiController?.loopMainThreadUntilIdle()
        }
    }

    /**
     * Returns an action that increments/decrements the value of a [NumberPicker] until reaching [value],
     * triggering [NumberPicker.OnValueChangeListener] if any.
     */
    fun setValueIncrementally(value: Int): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.instanceOf(NumberPicker::class.java)
        override fun getDescription(): String = "Incrementally set number picker value to $value"

        override fun perform(uiController: UiController?, view: View?) {
            uiController?.loopMainThreadUntilIdle()

            val picker = checkPreconditions(view, value, description)

            while (picker.value > value) {
                mDecrement.perform(uiController, view)
                uiController?.loopMainThreadUntilIdle()
            }
            while (picker.value < value) {
                mIncrement.perform(uiController, view)
                uiController?.loopMainThreadUntilIdle()
            }
        }
    }

    /**
     * Matches a [NumberPicker] that has value [value]
     */
    fun withValue(value: Int): Matcher<View> =
        object : BoundedMatcher<View, NumberPicker>(NumberPicker::class.java) {
            override fun describeTo(description: Description?) {
                description?.appendText("is a NumberPicker with value $value")
            }

            override fun matchesSafely(item: NumberPicker?): Boolean = item?.value == value
        }
}