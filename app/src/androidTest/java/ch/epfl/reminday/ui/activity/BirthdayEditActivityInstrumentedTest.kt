package ch.epfl.reminday.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.testutils.NumberPickerTestUtils.setValueByJumping
import ch.epfl.reminday.testutils.NumberPickerTestUtils.withValue
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.ArgumentNames
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class BirthdayEditActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // inject Dao inside test to perform setups/verifications
    @Inject
    lateinit var dao: BirthdayDao

    @Before
    fun init() {
        Intents.init()
        hiltRule.inject()
    }

    @After
    fun clear() {
        Intents.release()
    }

    private fun launch(
        birthday: Birthday? = null,
        mode: BirthdayEditActivity.Mode? = null,
        testMethod: suspend (ActivityScenario<BirthdayEditActivity>) -> Unit,
    ) {
        val intent = Intent(getApplicationContext(), BirthdayEditActivity::class.java)
        birthday?.let { intent.putExtra(ArgumentNames.BIRTHDAY, it) }
        mode?.let { intent.putExtra(ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL, it.ordinal) }

        ActivityScenario.launch<BirthdayEditActivity>(intent).use {
            runBlocking {
                testMethod.invoke(it)
            }
        }
    }

    private val onName: ViewInteraction get() = onView(withId(R.id.name_edit_text))
    private val onDay: ViewInteraction get() = onView(withId(R.id.day))
    private val onMonth: ViewInteraction get() = onView(withId(R.id.month))
    private val onYear: ViewInteraction get() = onView(withId(R.id.year))
    private val onConfirm: ViewInteraction get() = onView(withId(R.id.confirm_button))

    @Test
    fun registersBirthdayInsideDB() = launch { scenario ->
        val expected = Mocks.birthday(yearKnown = true)

        onName.perform(
            replaceText(expected.personName),
            closeSoftKeyboard(),
        )

        onYear.perform(setValueByJumping(expected.year!!.value))
        onMonth.perform(setValueByJumping(expected.monthDay.monthValue))
        onDay.perform(setValueByJumping(expected.monthDay.dayOfMonth))

        onConfirm.perform(click())

        assertEquals(Activity.RESULT_OK, scenario.result.resultCode)
        assertEquals(expected, dao.findByName(expected.personName))
    }

    @Test
    fun backArrowCancelsModifications() = launch { scenario ->
        val expected = Mocks.birthday(yearKnown = true)

        onName.perform(
            replaceText(expected.personName),
            closeSoftKeyboard(),
        )

        onYear.perform(setValueByJumping(expected.year!!.value))
        onMonth.perform(setValueByJumping(expected.monthDay.monthValue))
        onDay.perform(setValueByJumping(expected.monthDay.dayOfMonth))

        Espresso.pressBackUnconditionally()

        assertEquals(Activity.RESULT_CANCELED, scenario.result.resultCode)
        assertEquals(null, dao.findByName(expected.personName))
    }

    @Test
    fun cantConfirmIfNameIsEmpty() = launch {
        onName.perform(
            replaceText("   "),
            closeSoftKeyboard(),
        )

        onConfirm.check(matches(not(isEnabled())))
    }

    @Test
    fun initiallyFillsFieldsWithBirthday() {
        val expected = Mocks.birthday(yearKnown = true)

        launch(expected) {
            onName.check(matches(withText(expected.personName)))

            onDay.check(matches(allOf(isDisplayed(), withValue(expected.monthDay.dayOfMonth))))
            onMonth.check(matches(allOf(isDisplayed(), withValue(expected.monthDay.monthValue))))
            onYear.check(matches(allOf(isDisplayed(), withValue(expected.year!!.value))))
        }
    }

    @Test
    fun overwritingBirthdayShowsConfirmationDialogAndCancellingItDoesNothing(): Unit = runBlocking {
        val initial = Mocks.birthday(yearKnown = false)
        val nextMonth = 1 + (initial.monthDay.monthValue + 1) % 12

        dao.insertAll(initial)

        launch(initial) {
            onMonth.perform(setValueByJumping(nextMonth))
            onConfirm.perform(click())

            onView(withText(R.string.birthday_will_be_overwritten)).check(matches(isDisplayed()))
            onView(withText(R.string.cancel)).perform(click())
        }

        assertEquals(initial, dao.findByName(initial.personName))
    }

    @Test
    fun overwritingBirthdayShowsConfirmationDialogAndConfirmingItCommits(): Unit = runBlocking {
        val initial = Mocks.birthday(yearKnown = false)
        val nextMonth = 1 + (initial.monthDay.monthValue + 1) % 12
        val modified = initial.copy(monthDay = initial.monthDay.withMonth(nextMonth))

        dao.insertAll(initial)

        launch(initial, BirthdayEditActivity.Mode.ADD) {
            onMonth.perform(setValueByJumping(nextMonth))
            onConfirm.perform(click())

            onView(withText(R.string.birthday_will_be_overwritten)).check(matches(isDisplayed()))
            onView(withText(R.string.confirm)).perform(click())
        }

        assertEquals(modified, dao.findByName(initial.personName))
    }

    @Test
    fun editModeDoesNotPromptUserForConfirmationOnOverwritingSameBirthday(): Unit = runBlocking {
        val initial = Mocks.birthday(yearKnown = false)
        val nextMonth = 1 + (initial.monthDay.monthValue + 1) % 12
        val modified = initial.copy(monthDay = initial.monthDay.withMonth(nextMonth))

        dao.insertAll(initial)

        launch(initial, BirthdayEditActivity.Mode.EDIT) {
            onMonth.perform(setValueByJumping(nextMonth))
            onConfirm.perform(click())
        }

        assertEquals(modified, dao.findByName(initial.personName))
    }

    @Test
    fun editModePromptsUserWhenTheyTryToOverwriteAnotherBirthday(): Unit = runBlocking {
        val faker = Mocks.makeFaker().artist.unique
        val initial = Mocks.birthday(yearKnown = false).copy(personName = faker.names())
        val existing = Mocks.birthday(yearKnown = true).copy(personName = faker.names())

        val modified = initial.copy(personName = existing.personName)

        dao.insertAll(initial, existing)

        launch(initial, BirthdayEditActivity.Mode.EDIT) {
            onName.perform(replaceText(modified.personName), closeSoftKeyboard())
            onConfirm.perform(click())

            onView(withText(R.string.birthday_will_be_overwritten)).check(matches(isDisplayed()))
            onView(withText(R.string.confirm)).perform(click())
        }

        assertNull(dao.findByName(initial.personName))
        assertEquals(modified, dao.findByName(existing.personName))
    }

    @Test
    fun editModeDoesNotDeleteExistingBirthdayWhenCancelled(): Unit = runBlocking {
        val faker = Mocks.makeFaker().artist.unique
        val initial = Mocks.birthday(yearKnown = false).copy(personName = faker.names())
        val existing = Mocks.birthday(yearKnown = true).copy(personName = faker.names())

        val modified = initial.copy(personName = existing.personName)

        dao.insertAll(initial, existing)

        launch(initial, BirthdayEditActivity.Mode.EDIT) {
            onName.perform(replaceText(modified.personName), closeSoftKeyboard())
            onConfirm.perform(click())

            onView(withText(R.string.birthday_will_be_overwritten)).check(matches(isDisplayed()))
            onView(withText(R.string.cancel)).perform(click())
        }

        assertEquals(initial, dao.findByName(initial.personName))
        assertEquals(existing, dao.findByName(existing.personName))
    }
}