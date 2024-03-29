package ch.epfl.reminday.ui.activity

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import ch.epfl.reminday.R
import ch.epfl.reminday.di.SecurityTestDI
import ch.epfl.reminday.security.PromptUserUnlock
import ch.epfl.reminday.testutils.MockitoMatchers.any
import ch.epfl.reminday.testutils.UITestUtils.assertNoUnverifiedIntentIgnoringBootstrap
import ch.epfl.reminday.util.constant.PreferenceNames
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever

@HiltAndroidTest
class StartActivityInstrumentedTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private fun launchStartActivity(testFun: (ActivityScenario<StartActivity>) -> Unit) {
        ActivityScenario.launch(StartActivity::class.java).use(testFun)
    }

    private val prompt: PromptUserUnlock
        get() = SecurityTestDI.prompt

    private val onButton: ViewInteraction
        get() = onView(withId(R.id.continue_button))

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun init() {
        Intents.init()
        SecurityTestDI.reset()

        context = getApplicationContext()
        prefs = context.getSharedPreferences(GENERAL_PREFERENCES, MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    @After
    fun clear() {
        prefs.edit().clear().apply()
        Intents.release()
    }

    @Test
    fun launchesMainActivityIfAuthenticationNotSupported() {
        whenever(prompt.canAuthenticate()).thenReturn(false)

        launchStartActivity {
            intended(hasComponent(MainActivity::class.java.name))
            assertNoUnverifiedIntentIgnoringBootstrap()

            runBlocking {
                verify(prompt, Mockito.times(0)).authenticate(any())
            }
        }
    }

    @Test
    fun promptsUserForUnlockIfAvailable(): Unit = runBlocking {
        whenever(prompt.canAuthenticate()).thenReturn(true)
        whenever(prompt.authenticate(any())).thenReturn(false, true)

        launchStartActivity {
            // no intent yet (first authentication failed)
            intended(hasComponent(MainActivity::class.java.name), Intents.times(0))

            // click on button -> 2nd authentication succeeds
            onButton.perform(click())
            intended(hasComponent(MainActivity::class.java.name), Intents.times(1))

            assertNoUnverifiedIntentIgnoringBootstrap()
        }
    }

    @Test
    fun launchesActivityIfUserUnlockNotRequired() {
        prefs.edit()
            .putBoolean(context.getString(R.string.prefs_require_user_unlock), false)
            .apply()
        whenever(prompt.canAuthenticate()).thenReturn(true)

        launchStartActivity {
            intended(hasComponent(MainActivity::class.java.name))
            assertNoUnverifiedIntentIgnoringBootstrap()

            runBlocking {
                verify(prompt, Mockito.times(0)).authenticate(any())
            }
        }
    }
}