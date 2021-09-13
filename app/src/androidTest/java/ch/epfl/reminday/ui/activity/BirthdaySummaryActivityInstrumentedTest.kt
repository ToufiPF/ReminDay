package ch.epfl.reminday.ui.activity

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.testutils.UITestUtils.onMenuItem
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@HiltAndroidTest
class BirthdaySummaryActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val formatter = DateFormatter.longFormatter(Locale.ENGLISH)

    @Before
    fun init() {
        Intents.init()
    }

    @After
    fun release() {
        Intents.release()
    }

    private fun launchBirthdaySummary(
        birthday: Birthday,
        test: (ActivityScenario<BirthdaySummaryActivity>) -> Unit
    ) {
        val intent = Intent(getApplicationContext(), BirthdaySummaryActivity::class.java)
        intent.putExtra(BIRTHDAY, birthday)

        ActivityScenario.launch<BirthdaySummaryActivity>(intent).use(test)
    }

    private val onName: ViewInteraction get() = onView(withId(R.id.name))
    private val onDate: ViewInteraction get() = onView(withId(R.id.date))


    @Test
    fun displaysNameAndDate() {
        val birthday = Mocks.birthday(yearKnown = true)

        launchBirthdaySummary(birthday) {
            onName.check(matches(withText(birthday.personName)))
            onDate.check(matches(withText(formatter.format(birthday.monthDay, birthday.year))))
        }
    }

    @Test
    fun editActionLaunchesBirthdayEditActivity() {
        val birthday = Mocks.birthday(yearKnown = false)

        launchBirthdaySummary(birthday) {
            onMenuItem(withText(R.string.edit_birthday_item_text)).perform(click())

            intended(
                allOf(
                    hasComponent(BirthdayEditActivity::class.java.name),
                    hasExtra(BIRTHDAY, birthday),
                    hasExtra(BIRTHDAY_EDIT_MODE_ORDINAL, BirthdayEditActivity.Mode.EDIT.ordinal),
                )
            )
        }
    }
}