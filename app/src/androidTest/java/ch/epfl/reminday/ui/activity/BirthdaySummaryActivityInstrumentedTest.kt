package ch.epfl.reminday.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.AdditionalInformationDao
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.di.BirthdayDatabaseTestDI
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.testutils.IdlingResources
import ch.epfl.reminday.testutils.UITestUtils.onMenuItem
import ch.epfl.reminday.testutils.UITestUtils.waitUntilPopulated
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject

@HiltAndroidTest
class BirthdaySummaryActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val formatter = DateFormatter.longFormatter(Locale.ENGLISH)

    @Inject
    lateinit var birthdayDao: BirthdayDao

    @Inject
    lateinit var infoDao: AdditionalInformationDao

    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()

        context = getApplicationContext()
        preferences = context.getSharedPreferences(GENERAL_PREFERENCES, Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
    }

    @After
    fun release() {
        Intents.release()
        preferences.edit().clear().commit()

        IdlingResources.unregisterAll()
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

    @Test
    fun deleteActionDoesDeleteBirthdayFromDaoAndCloseActivity(): Unit = runBlocking {
        preferences.edit()
            .putBoolean(context.getString(R.string.prefs_show_delete_confirmation), false)
            .commit()

        BirthdayDatabaseTestDI.fillIn(birthdayDao)
        val bDay = birthdayDao.getAll().first()

        launchBirthdaySummary(bDay) {
            onMenuItem(withText(R.string.delete_birthday_item_text)).perform(click())

            onIdle()

            assertThat(it.result, hasResultCode(Activity.RESULT_CANCELED))
        }

        assertEquals(infoDao.getInfoForName(bDay.personName).size, 0)
        assertFalse(birthdayDao.getAll().contains(bDay))
    }

    @Test
    fun additionalInformationAreDisplayed(): Unit = runBlocking {
        val bDay = Mocks.birthday(yearKnown = true)
        val infos = Array(3) { Mocks.additionalInfo(bDay.personName) }

        birthdayDao.insertAll(bDay)
        infoDao.insertAll(*infos)

        launchBirthdaySummary(bDay) { scenario ->
            scenario.onActivity { activity ->
                IdlingResources.register(activity.recyclerIdlingResource)
            }

            onIdle()

            onView(withId(R.id.additional_info_recycler))
                .perform(waitUntilPopulated(3))

            infos.forEach {
                onView(withId(R.id.additional_info_recycler))
                    .check(matches(hasDescendant(withText(it.data))))
            }
        }
    }
}
