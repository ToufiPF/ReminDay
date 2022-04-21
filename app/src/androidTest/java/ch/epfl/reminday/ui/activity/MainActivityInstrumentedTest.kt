package ch.epfl.reminday.ui.activity

import android.Manifest.permission.READ_CONTACTS
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract.CommonDataKinds.Event.START_DATE
import android.provider.ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.data.contacts.ContactQuery
import ch.epfl.reminday.data.contacts.ContactQueryDI
import ch.epfl.reminday.testutils.IdlingResources
import ch.epfl.reminday.testutils.MockitoMatchers.any
import ch.epfl.reminday.testutils.MockitoMatchers.anyNullable
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL
import ch.epfl.reminday.viewmodel.activity.MainViewModel
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDate
import javax.inject.Inject
import org.mockito.Mockito.`when` as whenever

@UninstallModules(ContactQueryDI::class)
@HiltAndroidTest
class MainActivityInstrumentedTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val scenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule(order = 2)
    val permissionRule = GrantPermissionRule.grant(READ_CONTACTS)!!


    @Inject
    lateinit var dao: BirthdayDao

    @BindValue
    @JvmField
    val contactQuery: ContactQuery = mock(ContactQuery::class.java)
    private lateinit var cursor: Cursor

    private lateinit var faker: Faker

    @Before
    fun init(): Unit = runBlocking {
        reset(contactQuery)
        cursor = mock(Cursor::class.java)

        whenever(
            contactQuery.query(
                any(),
                any(),
                any(),
                any(),
                anyNullable(),
                anyNullable(),
            )
        ).thenReturn(cursor)

        Intents.init()
        hiltRule.inject()

        faker = Mocks.makeFaker()
    }

    @After
    fun clear() {
        Intents.release()
        IdlingResources.unregisterAll()
    }

    @Test
    fun addBirthdayButtonLaunchesAddBirthdayActivity() {
        UITestUtils.onMenuItem(withText(R.string.add_birthday_item_text))
            .perform(click())

        intended(
            allOf(
                hasComponent(BirthdayEditActivity::class.java.name),
                hasExtra(BIRTHDAY_EDIT_MODE_ORDINAL, BirthdayEditActivity.Mode.ADD.ordinal),
            )
        )
    }

    @Test
    fun birthdayListIsDisplayed(): Unit = runBlocking {
        dao.insertAll(Mocks.birthday(yearKnown = false))

        onView(withId(R.id.birthday_list_recycler))
            .perform(UITestUtils.waitUntilPopulated())
            .check(matches(isDisplayed()))
    }

    @Test
    fun importFromContactsInsertsThemIntoDao(): Unit = runBlocking {
        scenarioRule.scenario.onActivity {
            IdlingResources.register(it.importIdlingResource)
        }

        // initialize mock Cursor
        whenever(cursor.getColumnIndex(any())).then {
            when (it.getArgument<String>(0)) {
                DISPLAY_NAME_PRIMARY -> 0
                START_DATE -> 1
                else -> -1
            }
        }
        // exactly 3 contacts provided
        whenever(cursor.moveToNext()).thenReturn(true, true, true, false)

        val bDays = Mocks.birthdays(3, yearKnown = { it == 0 })
        val names = bDays.map { it.personName }
        val dates = bDays.map {
            if (it.isYearKnown) LocalDate.of(it.year!!.value, it.monthDay.month, it.monthDay.dayOfMonth).toString()
            else it.monthDay.toString()
        }
        whenever(cursor.getString(0)).thenReturn(names[0], names[1], names[2]).thenThrow()
        whenever(cursor.getString(1)).thenReturn(dates[0], dates[1], dates[2]).thenThrow()

        UITestUtils.onMenuItem(withText(R.string.import_from_contacts_item_text))
            .perform(click())

        onView(withText(R.string.confirm)).perform(click())

        Espresso.onIdle()

        verify(cursor, times(2)).getColumnIndex(any())
        verify(cursor, times(4)).moveToNext()
        verify(cursor, times(6)).getString(anyInt())
        verifyNoMoreInteractions(cursor)

        val actualBDays = dao.getAll()
        assertThat(actualBDays.size, `is`(3))
        bDays.zip(actualBDays).forEach { pair ->
            assertThat(pair.second, `is`(pair.first))
        }
    }

    @Test
    fun cancellingDoesNotInfluenceDAO(): Unit = runBlocking {
        scenarioRule.scenario.onActivity {
            IdlingResources.register(it.importIdlingResource)
        }

        // initialize mock Cursor
        whenever(cursor.getColumnIndex(any())).then {
            when (it.getArgument<String>(0)) {
                DISPLAY_NAME_PRIMARY -> 0
                START_DATE -> 1
                else -> -1
            }
        }
        whenever(cursor.moveToNext()).thenReturn(false)

        UITestUtils.onMenuItem(withText(R.string.import_from_contacts_item_text))
            .perform(click())

        onView(withText(R.string.cancel)).perform(click())

        // there should be no interactions at all
        verifyNoInteractions(cursor)
        assertTrue(dao.getAll().isEmpty())
    }

    @Test
    fun triesAgainToImportWhenPermissionIsGranted() {
        scenarioRule.scenario.onActivity {
            IdlingResources.register(it.importIdlingResource)
        }

        scenarioRule.scenario.onActivity {
            val code = MainViewModel.READ_CONTACTS_PERMISSION_CODE
            val permissions = arrayOf(READ_CONTACTS)
            val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)
            it.onRequestPermissionsResult(code, permissions, grantResults)
        }

        // assert that we're showing the confirmation dialog
        onView(withText(R.string.import_from_contacts_are_you_sure))
            .check(matches(isDisplayed()))
    }
}