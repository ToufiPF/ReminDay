package ch.epfl.reminday.testutils

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import ch.epfl.reminday.EmptyHiltTestActivity
import ch.epfl.reminday.EmptyRegularTestActivity
import ch.epfl.reminday.R
import ch.epfl.reminday.testutils.SafeFragmentScenario.Companion.launchInHiltContainer
import ch.epfl.reminday.testutils.SafeFragmentScenario.Companion.launchInRegularContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

/**
 * FragmentScenario from the androidx.fragment:fragment-testing library is not safe
 * because it doesn't close the scenario opened under the hood.
 *
 * As a workaround, use this class that's basically equivalent.
 * Start a scenario for a regular Fragment with [launchInRegularContainer] or alternatively,
 * start a scenario for a Hilt Fragment with [launchInHiltContainer].
 *
 * Use [onFragment] to operate directly on the instantiated fragment.
 */
class SafeFragmentScenario<F : Fragment> @PublishedApi internal constructor(
    private val scenario: ActivityScenario<out AppCompatActivity>
) {

    companion object {
        @PublishedApi
        internal const val SAFE_FRAGMENT_TEST_TAG = "Safe_Fragment_Scenario_Tag"

        /**
         * Launches the fragment in a regular container.
         *
         * @param fragmentArgs a [Bundle], the arguments to pass to the fragment (can be null)
         * @param themeResId the Theme's id
         * @param instantiate a factory lambda to create your fragment
         * if it has no zero arguments constructors
         * @param testFunction the method that runs the test. When this function exits, the scenario is closed.
         *
         * You must close the scenario after your test case. To ensure this, the test function is required as a parameter
         *
         * @see [onFragment] to operate on your fragment inside the scenario
         */
        inline fun <reified T : Fragment> launchInRegularContainer(
            fragmentArgs: Bundle? = null,
            @StyleRes themeResId: Int = R.style.Theme_ReminDay,
            noinline instantiate: (() -> T)? = null,
            testFunction: (scenario: SafeFragmentScenario<T>) -> Unit
        ) {
            val startActivityIntent = Intent.makeMainActivity(
                ComponentName(getApplicationContext(), EmptyRegularTestActivity::class.java)
            )
            launchInternal(startActivityIntent, fragmentArgs, themeResId, instantiate, testFunction)
        }

        /**
         * Launches the fragment in a Hilt container. Just like when testing an Hilt Activity,
         * you must declare the [HiltAndroidRule] in your test class and annotate it with [HiltAndroidTest].
         *
         * @param fragmentArgs a [Bundle], the arguments to pass to the fragment (can be null)
         * @param themeResId the Theme's id
         * @param instantiate a factory lambda to create your fragment
         * if it has no zero arguments constructors
         * @param testFunction the method that runs the test. When this function exits, the scenario is closed.
         *
         * @see [onFragment] to operate on your fragment inside the scenario
         */
        inline fun <reified T : Fragment> launchInHiltContainer(
            fragmentArgs: Bundle? = null,
            @StyleRes themeResId: Int = R.style.Theme_ReminDay,
            noinline instantiate: (() -> T)? = null,
            testFunction: (scenario: SafeFragmentScenario<T>) -> Unit
        ) {
            val startActivityIntent = Intent.makeMainActivity(
                ComponentName(getApplicationContext(), EmptyHiltTestActivity::class.java)
            )
            launchInternal(startActivityIntent, fragmentArgs, themeResId, instantiate, testFunction)
        }

        @PublishedApi
        internal inline fun <reified T : Fragment> launchInternal(
            activityIntent: Intent,
            fragmentArgs: Bundle?,
            @StyleRes themeResId: Int,
            noinline instantiate: (() -> T)?,
            test: (scenario: SafeFragmentScenario<T>) -> Unit
        ) {
            val scenario = ActivityScenario.launch<AppCompatActivity>(activityIntent)

            // attach fragment to activity
            scenario.onActivity { activity ->
                activity.setTheme(themeResId)

                val fragment: Fragment =
                    if (instantiate != null) instantiate()
                    else activity.supportFragmentManager.fragmentFactory.instantiate(
                        T::class.java.classLoader!!,
                        T::class.java.name
                    ) as T

                fragment.arguments = fragmentArgs

                activity.supportFragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, fragment, SAFE_FRAGMENT_TEST_TAG)
                    .commitNow()
            }

            // create the safe scenario wrapper, feed it to test function
            // and then close scenario
            scenario.use {
                val safeScenario = SafeFragmentScenario<T>(scenario)
                test.invoke(safeScenario)
            }
        }
    }

    /**
     * Inside the test method given to [launchInRegularContainer] or [launchInHiltContainer],
     * call this method to execute an action on your fragment.
     * Acts as a shorthand to [ActivityScenario.onActivity] + retrieving your fragment and executing [action] on it.
     * @param action the action to execute on your fragment
     */
    fun onFragment(action: (F) -> Unit) {
        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentByTag(SAFE_FRAGMENT_TEST_TAG)
            requireNotNull(fragment) { "Fragment was already detached." }

            @Suppress("UNCHECKED_CAST")
            action.invoke(fragment as F)
        }
    }
}