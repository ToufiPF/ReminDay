package ch.epfl.reminday.ui.activity

import androidx.biometric.BiometricManager
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

class StartActivityInstrumentedTest {

    @get:Rule(order = 0)
    val scenarioRule = ActivityScenarioRule(StartActivity::class.java)

    @get:Rule(order = 1)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var manager: BiometricManager

    @Before
    fun init() {

    }

    @Test
    fun launchesMainActivity() {

    }
}