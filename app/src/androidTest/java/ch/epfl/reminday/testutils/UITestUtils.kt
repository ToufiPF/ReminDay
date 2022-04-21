package ch.epfl.reminday.testutils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.StringDescription
import java.util.concurrent.TimeoutException

@Suppress("UNUSED")
object UITestUtils {

    private const val BOOSTRAP_ACTIVITY_NAME =
        "androidx.test.core.app.InstrumentationActivityInvoker\$BootstrapActivity"

    /**
     * Asserts that there's no unverified intents, but ignores an eventual intent with component "BootstrapActivity"
     * which is the activity used to boot most tests.
     * @see Intents.assertNoUnverifiedIntents
     */
    fun assertNoUnverifiedIntentIgnoringBootstrap() {
        if (Intents.getIntents().find { it.component?.className == BOOSTRAP_ACTIVITY_NAME } != null)
            Intents.intended(IntentMatchers.hasComponent(BOOSTRAP_ACTIVITY_NAME))

        Intents.assertNoUnverifiedIntents()
    }

    /**
     * Returns a [ViewInteraction] for a menu item matching [matcher].
     * Overflows the menu/action bar if needed
     * and then uses [Espresso.onView] with the given matcher.
     *
     * @param matcher [Matcher] menu item matcher
     * @return [ViewInteraction] for the given item.
     */
    fun onMenuItem(matcher: Matcher<View>): ViewInteraction {
        try {
            val context: Context = ApplicationProvider.getApplicationContext()
            Espresso.openActionBarOverflowOrOptionsMenu(context)
        } catch (ignored: Exception) {
            // there may be no menu overflow, ignore
        }

        return Espresso.onView(matcher)
    }

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
     * Action that blocks the test until the [RecyclerView]
     * is finished loading and has at least [minChildren] children.
     * @param minChildren [Int] the minimum number of children to wait for. 1 by default.
     * @param timeout [Long] the maximum time to wait, in milliseconds. 5s by default.
     */
    fun waitUntilPopulated(minChildren: Int = 1, timeout: Long = 5000L): ViewAction =
        object : ViewAction {
            override fun getConstraints(): Matcher<View> =
                Matchers.instanceOf(RecyclerView::class.java)

            override fun getDescription(): String =
                "Waits until the recycler view has finished loading and has at least $minChildren children."

            override fun perform(uiController: UiController?, view: View?) {
                val recycler = view!! as RecyclerView

                uiController?.loopMainThreadUntilIdle()

                val stop = System.currentTimeMillis() + timeout
                while (recycler.hasPendingAdapterUpdates() ||
                    recycler.adapter!!.itemCount < minChildren
                ) {
                    uiController?.loopMainThreadForAtLeast(50)

                    if (System.currentTimeMillis() > stop)
                        throw PerformException.Builder()
                            .withActionDescription(description)
                            .withViewDescription(HumanReadables.describe(recycler))
                            .withCause(TimeoutException("Waited for $timeout ms and recycler is still loading/doesn't have $minChildren children"))
                            .build()
                }
            }
        }
}