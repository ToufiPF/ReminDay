package ch.epfl.reminday.testutils

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

/**
 * Use this scenario to wrap a single [View] into a scenario usable in a test.
 */
class SafeViewScenario<V : View> @PublishedApi internal constructor(
    private val scenario: SafeFragmentScenario<*>
) {
    companion object {
        private const val TEST_TAG = "TESTED_VIEW_TAG"

        /**
         * Launches the view in a regular container, without the hilt wrapper.
         * @param viewFactory the factory to create the view
         * @param testFunction the function executing the tests
         * @see onView to interact directly with the view
         */
        inline fun <V : View> launchInRegularContainer(
            noinline viewFactory: (Context) -> V,
            testFunction: (SafeViewScenario<V>) -> Unit
        ) = SafeFragmentScenario.launchInRegularContainer(
            instantiate = { RegularFragment(viewFactory) },
            testFunction = {
                val scenario = SafeViewScenario<V>(it)
                testFunction.invoke(scenario)
            }
        )

        /**
         * Launches the view in an Hilt container. Just like when testing an Hilt Activity,
         * you must declare the [HiltAndroidRule] in your test class and annotate it with [HiltAndroidTest].
         * @param viewFactory the factory to create the view
         * @param testFunction the function executing the tests
         * @see onView to interact directly with the view
         */
        inline fun <V : View> launchInHiltContainer(
            noinline viewFactory: (Context) -> V,
            testFunction: (SafeViewScenario<V>) -> Unit
        ) = SafeFragmentScenario.launchInHiltContainer(
            instantiate = { HiltFragment(viewFactory) },
            testFunction = {
                val scenario = SafeViewScenario<V>(it)
                testFunction.invoke(scenario)
            }
        )
    }

    @PublishedApi
    internal open class RegularFragment(
        private val viewFactory: (Context) -> View,
    ) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val context = container?.context!!

            val scrollView = ScrollView(context)
            scrollView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            val testedView = viewFactory.invoke(context)
            testedView.tag = TEST_TAG
            scrollView.addView(testedView)

            return scrollView
        }
    }

    @AndroidEntryPoint
    @PublishedApi
    internal class HiltFragment(viewFactory: (Context) -> View) : RegularFragment(viewFactory)

    /**
     * Executes [action] on the tested view.
     * Similarly to [SafeFragmentScenario.onFragment] or [ActivityScenario.onActivity],
     * do not attempt to access the view externally to this function
     * @param action the action to execute
     */
    fun onView(action: (V) -> Unit) {
        scenario.onFragment {
            val testedView: V =
                requireNotNull(it.view?.findViewWithTag(TEST_TAG)) { "Tested view was not found!" }

            action.invoke(testedView)
        }
    }
}