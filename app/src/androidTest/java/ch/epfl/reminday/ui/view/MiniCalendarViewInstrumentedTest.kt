package ch.epfl.reminday.ui.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.TableLayout
import android.widget.TableRow
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.SafeFragmentScenario
import ch.epfl.reminday.SafeViewScenario
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.time.Month
import java.time.MonthDay

@HiltAndroidTest
class MiniCalendarViewInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun displaysDayAndMonth() {
        val factory = { context: Context ->
            val view = MiniCalendarView(context)
            view.monthDay = MonthDay.of(Month.JANUARY, 31)
            view
        }

        SafeViewScenario.launchInHiltContainer(factory) {
            onView(withId(R.id.background)).check(matches(isDisplayed()))
            onView(withId(R.id.month)).check(matches(allOf(isDisplayed(), withText("Jan"))))
            onView(withId(R.id.day)).check(matches(allOf(isDisplayed(), withText("31"))))
        }
    }

    @Test
    fun nullMonthDayShowsEmptyCalendar() {
        val factory = { context: Context -> MiniCalendarView(context) }

        SafeViewScenario.launchInHiltContainer(factory) {
            onView(withId(R.id.background)).check(matches(isDisplayed()))
            onView(withId(R.id.month)).check(matches(withText(Matchers.isEmptyOrNullString())))
            onView(withId(R.id.day)).check(matches(withText(Matchers.isEmptyOrNullString())))
        }
    }

    @Test
    @Ignore("This test is only there to demo the calendar for each month")
    fun showAllMonths() {
        val views = mutableListOf<(Context) -> View>()
        for (i in 1..12) {
            val factory = { context: Context ->
                val view = MiniCalendarView(context)
                view.monthDay = MonthDay.of(i, 1 + 2 * i)
                view
            }
            views.add(factory)
        }

        SafeFragmentScenario.launchInHiltContainer(
            instantiate = { TestFragment(views) },
            testFunction = {
                runBlocking { delay(60000L) }
            }
        )
    }

    @AndroidEntryPoint
    class TestFragment(
        private val viewFactories: List<(Context) -> View>
    ) : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val layout = TableLayout(container?.context!!)
            layout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            layout.orientation = TableLayout.VERTICAL

            val rowSize = 3
            viewFactories.chunked(rowSize).forEach { chunk ->
                val row = TableRow(layout.context!!)
                row.setPadding(10, 10, 10, 10)
                chunk.forEach { factory ->
                    row.addView(factory.invoke(layout.context!!))
                }
                layout.addView(row)
            }

            for (i in 0 until minOf(rowSize, viewFactories.size))
                layout.setColumnStretchable(i, true)

            return layout
        }
    }
}