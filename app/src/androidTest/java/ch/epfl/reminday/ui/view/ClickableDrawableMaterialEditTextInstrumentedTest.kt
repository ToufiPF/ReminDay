package ch.epfl.reminday.ui.view

import android.view.View
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.SafeFragmentScenario
import ch.epfl.reminday.testutils.UITestUtils.clickOnCompoundDrawable
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.BOTTOM
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.LEFT
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.RIGHT
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.TOP
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ClickableDrawableMaterialEditTextInstrumentedTest {

    class TestFragment : Fragment(R.layout.activity_birthday_edit_info_item)

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
