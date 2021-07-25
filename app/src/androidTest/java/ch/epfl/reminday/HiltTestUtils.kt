/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.epfl.reminday

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

/**
 * launchFragmentInContainer from the androidx.fragment:fragment-testing library
 * is NOT possible to use right now as it uses a hardcoded Activity under the hood
 * (i.e. EmptyFragmentActivity) which is not annotated with @AndroidEntryPoint.
 *
 * As a workaround, use this function that is equivalent. It requires you to add
 * [EmptyHiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
 * as can be found in this project.
 *
 * @param fragmentArgs [Bundle] the arguments to pass to the fragment, can be null
 * @param themeResId [Int] the style id to use
 * @param instantiate a lambda to create the fragment.
 * May be null in which case the default no arg constructor will be used.
 * @return [ActivityScenario] on a [EmptyHiltTestActivity] with your fragment injected inside
 *
 * @see [onFragment] to operate on your fragment inside the scenario
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.Theme_ReminDay,
    noinline instantiate: (() -> T)? = null
): ActivityScenario<EmptyHiltTestActivity> {
    val startActivityIntent = Intent.makeMainActivity(
        ComponentName(
            ApplicationProvider.getApplicationContext(),
            EmptyHiltTestActivity::class.java
        )
    ).putExtra(
        "androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY",
        themeResId
    )

    val scenario = ActivityScenario.launch<EmptyHiltTestActivity>(startActivityIntent)
    scenario.onActivity { activity ->
        val fragment: Fragment = instantiate?.invoke()
            ?: activity.supportFragmentManager.fragmentFactory
                .instantiate(T::class.java.classLoader!!, T::class.java.name)

        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "INITIAL_TRANSACTION")
            .commitNow()
    }
    return scenario
}

/**
 * On a scenario returned by [launchFragmentInHiltContainer], call this method
 * to execute an action on your fragment.
 * Acts as a shorthand to [ActivityScenario.onActivity] + retrieving your fragment and executing [action] on it.
 * @param position [Int] the position of the fragment in the [EmptyHiltTestActivity], usually 0
 * @param action the action to execute on your fragment
 */
inline fun <reified T : Fragment> ActivityScenario<EmptyHiltTestActivity>.onFragment(
    position: Int = 0,
    crossinline action: (T) -> Unit
) {
    onActivity { activity ->
        val fragment = activity.supportFragmentManager.fragments[position] as T
        action.invoke(fragment)
    }
}
