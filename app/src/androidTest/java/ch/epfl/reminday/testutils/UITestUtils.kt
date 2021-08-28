package ch.epfl.reminday.testutils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.StringDescription
import java.util.concurrent.TimeoutException

object UITestUtils {
    /**
     * Waits for a view matching [viewMatcher] to appear in the hierarchy.
     *
     * @param viewMatcher the [Matcher] the view must satisfy
     * @param timeout max allowed time for the view to appear, in milliseconds
     */
    fun waitForView(
        viewMatcher: Matcher<View>,
        timeout: Long = 5000L,
    ) {
        Espresso.onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return Matchers.any(View::class.java)
            }

            override fun getDescription(): String {
                val matcherDescription = StringDescription()
                viewMatcher.describeTo(matcherDescription)
                return "wait for a specific view <$matcherDescription> to be displayed"
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()

                val startTime = System.currentTimeMillis()
                val endTime = startTime + timeout

                do {
                    val viewVisible = TreeIterables.breadthFirstViewTraversal(view)
                        .any { viewMatcher.matches(it) && isDisplayed().matches(it) }

                    if (viewVisible) return

                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < endTime)

                // Timeout happens.
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(TimeoutException("Waited for $timeout ms and view did not appear"))
                    .build()
            }
        })
    }

    /**
     * Action that blocks the test until the [RecyclerView] is finished loading.
     * @param timeout [Long] the maximum time to wait
     */
    fun waitUntilLoadingCompleted(timeout: Long = 5000L): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> =
            Matchers.instanceOf(RecyclerView::class.java)

        override fun getDescription(): String =
            "Waits until the recycler view has finished loading."

        override fun perform(uiController: UiController?, view: View?) {
            val recycler = view!! as RecyclerView

            uiController?.loopMainThreadUntilIdle()

            val stop = System.currentTimeMillis() + timeout
            while (recycler.hasPendingAdapterUpdates()) {
                uiController?.loopMainThreadForAtLeast(50)

                if (System.currentTimeMillis() > stop)
                    throw PerformException.Builder()
                        .withActionDescription(description)
                        .withViewDescription(HumanReadables.describe(recycler))
                        .withCause(TimeoutException("Waited for $timeout ms and recycler has not finished loading"))
                        .build()
            }
        }
    }
}