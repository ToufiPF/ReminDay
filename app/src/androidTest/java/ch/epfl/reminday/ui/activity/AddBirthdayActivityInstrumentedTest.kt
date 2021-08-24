package ch.epfl.reminday.ui.activity

import android.app.Activity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.testutils.NumberPickerTestUtils.setValueByJumping
import ch.epfl.reminday.utils.Mocks
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class AddBirthdayActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val scenarioRule = ActivityScenarioRule(AddBirthdayActivity::class.java)

    // inject Dao inside test to perform verifications
    @Inject
    lateinit var dao: BirthdayDao

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun clear() {
        Intents.release()
    }

    @Test
    fun registersBirthdayInsideDB(): Unit = runBlocking {
        val expected = Mocks.birthday(yearKnown = true)

        onView(withId(R.id.name_edit_text)).perform(
            replaceText(expected.personName),
            closeSoftKeyboard(),
        )

        onView(withId(R.id.year)).perform(setValueByJumping(expected.year!!.value))
        onView(withId(R.id.month)).perform(setValueByJumping(expected.monthDay.monthValue))
        onView(withId(R.id.day)).perform(setValueByJumping(expected.monthDay.dayOfMonth))

        onView(withId(R.id.confirm)).perform(click())

        assertEquals(Activity.RESULT_CANCELED, scenarioRule.scenario.result)
        assertEquals(expected, dao.findByName(expected.personName))
    }
}