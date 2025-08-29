package ch.epfl.reminday.background

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.data.birthday.BirthdayDatabase
import ch.epfl.reminday.di.BirthdayDatabaseTestDI
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.testutils.UITestUtils.waitAndFind
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.PreferenceNames.BACKGROUND_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.BackgroundPreferenceNames.LAST_BIRTHDAY_CHECK
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.AfterClass
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import java.util.Locale

class CheckBirthdayNotifierTest {
    companion object {
        private lateinit var context: Context
        private lateinit var db: BirthdayDatabase
        private lateinit var dao: BirthdayDao
        private val locale = Locale.ENGLISH

        private lateinit var preferences: SharedPreferences

        @BeforeClass
        @JvmStatic
        fun initWorkerConfig() {
            context = getApplicationContext()
            db = BirthdayDatabaseTestDI.provideDb(context)
            dao = BirthdayDatabaseTestDI.provideBirthdayDao(db)

            preferences = context.getSharedPreferences(BACKGROUND_PREFERENCES, MODE_PRIVATE)
        }

        @AfterClass
        @JvmStatic
        fun cleanUpClass() {
            UITestUtils.clearAllNotifications()

            preferences.edit()
                .clear()
                .apply()
        }

        private fun getString(@StringRes id: Int, vararg args: Any): String =
            context.getString(id, *args)
    }

    private fun expectedTitle(birthday: Birthday): String =
        getString(R.string.notif_title, birthday.personName)

    private fun expectedText(birthday: Birthday): String {
        val todayYear = LocalDate.now().year
        var text = getString(
            R.string.notif_text,
            formatter.format(birthday.monthDay, null),
            birthday.personName
        )
        if (birthday.isYearKnown)
            text += getString(
                R.string.notif_additional_text_year_known,
                todayYear - birthday.year!!.value
            )
        return text
    }

    private lateinit var formatter: DateFormatter
    private lateinit var faker: Faker

    @get:Rule
    val permissionRule = GrantPermissionRule.grant("android.permission.POST_NOTIFICATIONS")!!

    @Before
    fun init() {
        BirthdayDatabaseTestDI.clear(dao)

        preferences.edit().clear().apply()
        formatter = DateFormatter.shortFormatter(locale)
        faker = Mocks.makeFaker()

        UITestUtils.clearAllNotifications()
    }

    @Test
    fun workerDoesNothingWhenNoBirthdayToday(): Unit = runBlocking {
        val now = LocalDate.now()
        val day = now.plusDays(10)
        val birthday = Birthday(
            personName = faker.animal.name(),
            monthDay = MonthDay.of(day.month, day.dayOfMonth),
            year = Year.of(2010)
        )
        dao.insertAll(birthday)

        val worker = CheckBirthdayNotifier(context, dao, formatter)
        worker.checkForBirthdayToday()

        // preferences should be updated
        assertThat(preferences.getString(LAST_BIRTHDAY_CHECK, null), `is`(now.toString()))

        // notification shouldn't be visible
        val device = UITestUtils.getUiDevice()
        device.openNotification()
        assertNull(waitAndFind(By.textContains(expectedTitle(birthday)), throwIfNotFound = false))
    }

    @Test
    fun workerDisplaysTwoNotificationsWhenTwoSimultaneousBirthdays(): Unit = runBlocking {
        val now = LocalDate.now()
        val birthdays = arrayOf(
            Birthday(
                personName = faker.animal.unique.name(),
                monthDay = MonthDay.of(now.month, now.dayOfMonth),
                year = Year.of(2010)
            ),
            Birthday(
                personName = faker.animal.unique.name(),
                monthDay = MonthDay.of(now.month, now.dayOfMonth),
                year = Year.of(2020)
            ),
        )
        dao.insertAll(*birthdays)
        preferences.edit()
            .putString(LAST_BIRTHDAY_CHECK, now.minusDays(1).toString())
            .apply()

        val worker = CheckBirthdayNotifier(context, dao, formatter)
        worker.checkForBirthdayToday()

        assertThat(preferences.getString(LAST_BIRTHDAY_CHECK, null), `is`(now.toString()))

        // 2 notifications should be visible
        val device = UITestUtils.getUiDevice()
        device.openNotification()
        birthdays.forEach { bDay ->
            waitAndFind(By.textContains(expectedTitle(bDay)), throwIfNotFound = true)
            waitAndFind(By.textContains(expectedText(bDay)), throwIfNotFound = true)
        }
    }

    @Test
    fun ifLastDayCheckedIsTodayDoNotDisplayAgainNotification(): Unit = runBlocking {
        val now = LocalDate.now()
        val birthday = Birthday(
            personName = faker.animal.name(),
            monthDay = MonthDay.of(now.month, now.dayOfMonth),
            year = null
        )
        dao.insertAll(birthday)
        preferences.edit()
            .putString(LAST_BIRTHDAY_CHECK, now.toString())
            .apply()

        val worker = CheckBirthdayNotifier(context, dao, formatter)
        worker.checkForBirthdayToday()

        // notification shouldn't be visible
        val device = UITestUtils.getUiDevice()
        device.openNotification()
        assertNull(waitAndFind(By.textContains(expectedTitle(birthday)), throwIfNotFound = false))
        assertNull(waitAndFind(By.textContains(expectedText(birthday)), throwIfNotFound = false))
    }
}
