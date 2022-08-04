package ch.epfl.reminday.testutils

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.*
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place
import com.google.android.material.textfield.TextInputEditText
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
            val context: Context = getApplicationContext()
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


    /**
     * Returns a [ViewAction] that clicks on the desired compound drawable in an [EditText]
     */
    fun clickOnCompoundDrawable(where: Place) = object : ViewAction {
        override fun perform(uiController: UiController?, view: View?) {
            uiController!!.loopMainThreadUntilIdle()
            val v = view as EditText

            val halfWidth = v.width / 2
            val halfHeight = v.height / 2

            val location = IntArray(2)
            v.getLocationOnScreen(location)
            var x = location[0].toFloat()
            var y = location[1].toFloat()
            when (where) {
                Place.LEFT -> {
                    x += (v.paddingLeft + v.totalPaddingLeft) / 2
                    y += halfHeight
                }
                Place.TOP -> {
                    x += halfWidth
                    y += (v.paddingTop + v.totalPaddingTop) / 2
                }
                Place.RIGHT -> {
                    x += v.width - (v.paddingRight + v.totalPaddingRight) / 2
                    y += halfHeight
                }
                Place.BOTTOM -> {
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
            Matchers.instanceOf(TextInputEditText::class.java),
            isDisplayed(),
        )

        override fun getDescription(): String =
            "Click on the ${where.name.lowercase()} compound drawable"
    }


    /**
     * Shortcut to get the [UiDevice].
     * @see UiDevice.getInstance
     */
    fun getUiDevice(): UiDevice =
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    /**
     * Clears all notifications and closes the Notification Panel if open
     */
    fun clearAllNotifications() {
        NotificationManagerCompat.from(getApplicationContext()).cancelAll()
        closeNotificationPanel()
    }

    /**
     * Closes the notification panel if open
     */
    fun closeNotificationPanel() {
        // Causes SecurityException in normal uses, but it's authorized in android tests
        @Suppress("DEPRECATION")
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        getApplicationContext<Context>().sendBroadcast(it)
    }

    /**
     * Waits for a [UiObject2] matching [matcher] to appear on the screen,
     * and returns it.
     * @param matcher the [BySelector] matcher to satisfy
     * @param timeout (Long) timeout in ms
     * @param throwIfNotFound (Boolean) whether to throw if timeout is exceeded,
     * or to simply return null
     */
    fun waitAndFind(
        matcher: BySelector,
        timeout: Long = 2000L,
        throwIfNotFound: Boolean = true
    ): UiObject2? {
        val device = getUiDevice()

        val found = device.wait(Until.hasObject(matcher), timeout) ?: false
        if (throwIfNotFound && !found)
            throw RuntimeException(
                "Waited ${timeout}ms for an object matching $matcher to appear, in vain."
            )
        return device.findObject(matcher)
    }
}