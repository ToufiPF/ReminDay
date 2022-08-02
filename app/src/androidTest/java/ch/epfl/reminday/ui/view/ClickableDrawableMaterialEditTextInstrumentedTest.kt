package ch.epfl.reminday.ui.view

import android.view.View
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.SafeFragmentScenario
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditTextInstrumentedTest.Place.*
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ClickableDrawableMaterialEditTextInstrumentedTest {

    class TestFragment : Fragment(R.layout.activity_birthday_edit_info_item)

    enum class Place {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    }

    private fun clickOnCompoundDrawable(where: Place) = object : ViewAction {
        override fun perform(uiController: UiController?, view: View?) {
            uiController!!.loopMainThreadUntilIdle()
            val v = view as TextInputEditText

            val halfWidth = v.width / 2
            val halfHeight = v.height / 2

            val location = IntArray(2)
            v.getLocationOnScreen(location)
            var x = location[0].toFloat()
            var y = location[1].toFloat()
            when (where) {
                LEFT -> {
                    x += (v.paddingLeft + v.totalPaddingLeft) / 2
                    y += halfHeight
                }
                TOP -> {
                    x += halfWidth
                    y += (v.paddingTop + v.totalPaddingTop) / 2
                }
                RIGHT -> {
                    x += v.width - (v.paddingRight + v.totalPaddingRight) / 2
                    y += halfHeight
                }
                BOTTOM -> {
                    x += halfWidth
                    y += v.height - (v.paddingBottom + v.totalPaddingBottom) / 2
                }
            }

            val coordinates = FloatArray(2)
            coordinates[0] = x
            coordinates[1] = y
            val precision = FloatArray(2) { 1.0f }

            uiController.loopMainThreadUntilIdle()
            val down = MotionEvents.sendDown(uiController, coordinates, precision).down
            uiController.loopMainThreadForAtLeast(200)
            MotionEvents.sendUp(uiController, down)
        }

        override fun getConstraints(): Matcher<View> = Matchers.allOf(
            instanceOf(TextInputEditText::class.java),
            isDisplayed(),
        )

        override fun getDescription(): String =
            "Click on the ${where.name.lowercase()} compound drawable"
    }


    private val clicked = AtomicInteger(0)

    @Before
    fun init() {
        clicked.set(0)
    }

    private fun runTest(testFunction: (SafeFragmentScenario<TestFragment>) -> Unit) {
        @Suppress("RemoveExplicitTypeArguments") // otherwise fragment.view doesn't compile
        SafeFragmentScenario.launchInRegularContainer<TestFragment> { scenario ->
            scenario.onFragment { fragment ->
                val editText: ClickableDrawableMaterialEditText =
                    fragment.view?.findViewById(R.id.additional_info_edit_text)!!

                editText.apply {
                    setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_add_circle_24,
                        R.drawable.ic_baseline_close_24,
                        R.drawable.ic_baseline_edit_calendar_24,
                        R.drawable.ic_home_black_24dp
                    )
                    hint = "Test text"
                    text = null
                }
            }

            testFunction.invoke(scenario)
        }
    }

    private val listener = View.OnClickListener {
        clicked.incrementAndGet()
    }

    @Test
    fun topRightBottomActionsArePerformed() = runTest { scenario ->
        scenario.onFragment { fragment ->
            val editText: ClickableDrawableMaterialEditText =
                fragment.view?.findViewById(R.id.additional_info_edit_text)!!

            editText.apply {
                setLeftDrawableClickListener(listener)
                setTopDrawableClickListener(listener)
                setRightDrawableClickListener(listener)
                setBottomDrawableClickListener(listener)
            }
        }

        listOf(LEFT, TOP, RIGHT, BOTTOM).forEach {
            clicked.set(0)

            onView(withId(R.id.additional_info_edit_text))
                .perform(clickOnCompoundDrawable(it))
            onIdle()
            assertEquals(1, clicked.get())
        }
    }

    @Test
    fun startActionIsPerformedWithEnglishLayout() = runTest { scenario ->
        scenario.onFragment { fragment ->
            val editText: ClickableDrawableMaterialEditText =
                fragment.view?.findViewById(R.id.additional_info_edit_text)!!

            editText.setStartDrawableClickListener(listener)
        }

        onView(withId(R.id.additional_info_edit_text))
            .perform(clickOnCompoundDrawable(LEFT))
        onIdle()
        assertEquals(1, clicked.get())
    }

    @Test
    fun endActionIsPerformedWhenClickingOnRightDrawable() = runTest { scenario ->
        scenario.onFragment { fragment ->
            val editText: ClickableDrawableMaterialEditText =
                fragment.view?.findViewById(R.id.additional_info_edit_text)!!

            editText.setEndDrawableClickListener(listener)
        }

        onView(withId(R.id.additional_info_edit_text))
            .perform(clickOnCompoundDrawable(RIGHT))
        onIdle()
        assertEquals(1, clicked.get())
    }

    @Test
    fun noActionPerformedWhenNoDrawable() = runTest { scenario ->
        scenario.onFragment { fragment ->
            val editText: ClickableDrawableMaterialEditText =
                fragment.view?.findViewById(R.id.additional_info_edit_text)!!

            editText.apply {
                setLeftDrawableClickListener(listener)
                setTopDrawableClickListener(listener)
                setRightDrawableClickListener(listener)
                setBottomDrawableClickListener(listener)

                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
        }

        listOf(LEFT, TOP, RIGHT, BOTTOM).forEach {
            onView(withId(R.id.additional_info_edit_text))
                .perform(clickOnCompoundDrawable(it))
            onIdle()
            assertEquals(0, clicked.get())
        }
    }

    @Test
    fun noActionPerformedWhenNoListener() = runTest {
        listOf(LEFT, TOP, RIGHT, BOTTOM).forEach {
            onView(withId(R.id.additional_info_edit_text))
                .perform(clickOnCompoundDrawable(it))
            onIdle()
            assertEquals(0, clicked.get())
        }
    }
}
