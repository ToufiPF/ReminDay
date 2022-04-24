package ch.epfl.reminday.background

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.uiautomator.By
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import ch.epfl.reminday.MainApplication.Companion.makeWorkManagerConfigurationBuilder
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.di.BirthdayDatabaseTestDI
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.testutils.UITestUtils
import ch.epfl.reminday.testutils.UITestUtils.waitAndFind
import ch.epfl.reminday.util.Mocks
import ch.epfl.reminday.util.constant.PreferenceNames.BACKGROUND_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.BackgroundPreferenceNames.LAST_BIRTHDAY_CHECK
import dagger.hilt.android.testing.HiltAndroidTest
import io.github.serpro69.kfaker.Faker
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.AfterClass
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import java.util.*

@HiltAndroidTest
class CheckBirthdaysWorkerTest {

    companion object {
        private lateinit var context: Context
        private lateinit var dao: BirthdayDao
        private val locale = Locale.ENGLISH

        private lateinit var testDriver: TestDriver
        private lateinit var factory: BirthdayWorkerFactory
        private lateinit var workManager: WorkManager

        private lateinit var preferences: SharedPreferences

        @BeforeClass
        @JvmStatic
        fun initWorkerConfig() {
            context = getApplicationContext()
            dao = BirthdayDatabaseTestDI.provideFakeBirthdayDao(context)

            val config = makeWorkManagerConfigurationBuilder(dao, locale)
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()
            WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

            testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
            factory = BirthdayWorkerFactory(dao, locale)
            workManager = WorkManager.getInstance(context)

            preferences = context.getSharedPreferences(BACKGROUND_PREFERENCES, Context.MODE_PRIVATE)
        }

        @AfterClass
        @JvmStatic
        fun cleanUpClass() {
            UITestUtils.clearAllNotifications()

            preferences.edit()
                .remove(LAST_BIRTHDAY_CHECK)
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
            formatter.format(birthday.monthDay, birthday.year),
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

    @Before
    fun init() {
        BirthdayDatabaseTestDI.clear(dao)

        preferences.edit()
            .remove(LAST_BIRTHDAY_CHECK)
            .apply()
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

        val worker = TestListenableWorkerBuilder<CheckBirthdaysWorker>(context)
            .setWorkerFactory(factory)
            .build()

        val res = worker.doWork()
        assertThat(res, `is`(ListenableWorker.Result.success()))
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

        val worker = TestListenableWorkerBuilder<CheckBirthdaysWorker>(context)
            .setWorkerFactory(factory)
            .build()

        val res = worker.doWork()
        assertThat(res, `is`(ListenableWorker.Result.success()))
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
    fun oneTimeWorkRequestDisplaysNotification(): Unit = runBlocking {
        val now = LocalDate.now()
        val birthday = Birthday(
            personName = faker.animal.name(),
            monthDay = MonthDay.of(now.month, now.dayOfMonth),
            year = null
        )
        dao.insertAll(birthday)

        val request = CheckBirthdaysWorker.enqueueOneTimeWorkRequest(context)
        testDriver.setAllConstraintsMet(request.id)
        testDriver.setInitialDelayMet(request.id)

        val device = UITestUtils.getUiDevice()
        device.openNotification()
        waitAndFind(By.textContains(expectedTitle(birthday)), throwIfNotFound = true)
        waitAndFind(By.textContains(expectedText(birthday)), throwIfNotFound = true)
    }

    @Test
    fun periodicWorkRequestDisplaysNotification(): Unit = runBlocking {
        val now = LocalDate.now()
        val birthday = Birthday(
            personName = faker.animal.name(),
            monthDay = MonthDay.of(now.month, now.dayOfMonth),
            year = null
        )
        dao.insertAll(birthday)

        val request = CheckBirthdaysWorker.enqueuePeriodicWorkRequest(context)
        testDriver.setAllConstraintsMet(request.id)
        testDriver.setInitialDelayMet(request.id)

        val device = UITestUtils.getUiDevice()
        device.openNotification()
        waitAndFind(By.textContains(expectedTitle(birthday)), throwIfNotFound = true)
        waitAndFind(By.textContains(expectedText(birthday)), throwIfNotFound = true)
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

        val request = CheckBirthdaysWorker.enqueueOneTimeWorkRequest(context)
        testDriver.setAllConstraintsMet(request.id)
        testDriver.setInitialDelayMet(request.id)

        // notification shouldn't be visible
        val device = UITestUtils.getUiDevice()
        device.openNotification()
        assertNull(waitAndFind(By.textContains(expectedTitle(birthday)), throwIfNotFound = false))
        assertNull(waitAndFind(By.textContains(expectedText(birthday)), throwIfNotFound = false))
    }
}