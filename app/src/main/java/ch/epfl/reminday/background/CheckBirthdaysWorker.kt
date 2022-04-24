package ch.epfl.reminday.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.util.constant.PreferenceNames.BACKGROUND_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.BackgroundPreferenceNames.LAST_BIRTHDAY_CHECK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class CheckBirthdaysWorker(
    private val birthdayDao: BirthdayDao,
    private val formatter: DateFormatter,
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "BirthdayNotificationsChannel"

        /**
         * Returns the notification id
         */
        fun getNotificationId(dayOfYear: Int, idx: Int): Int =
            (idx shl 10) or dayOfYear

        private fun makePeriodicWorkRequest(): PeriodicWorkRequest {
            val now = LocalDateTime.now()
            val todayAt8 = now.withHour(8).truncatedTo(ChronoUnit.HOURS)
            val future =
                if (now.isBefore(todayAt8)) todayAt8
                else todayAt8.plusDays(1)

            // set initial delay to do the check on the next 8:00
            val initialDelay = now.until(future, ChronoUnit.MINUTES)

            return PeriodicWorkRequestBuilder<CheckBirthdaysWorker>(
                1,
                TimeUnit.DAYS,
                2,
                TimeUnit.HOURS
            ).setInitialDelay(initialDelay, TimeUnit.MINUTES).build()
        }

        /**
         * Enqueues a [PeriodicWorkRequest] ([CheckBirthdaysWorker]) to the [WorkManager].
         * There is an initial delay, so that the next check is performed at 8:00 in the future.
         * Then the check repeats every day.
         * If a periodic [CheckBirthdaysWorker] was already scheduled, it will be replaced.
         * @param appContext application context
         * @return [PeriodicWorkRequest] enqueued work request (mainly for tests)
         */
        fun enqueuePeriodicWorkRequest(appContext: Context): PeriodicWorkRequest {
            val workRequest = makePeriodicWorkRequest()
            WorkManager.getInstance(appContext).apply {
                enqueueUniquePeriodicWork(
                    CheckBirthdaysWorker::class.java.name,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
            }
            return workRequest
        }

        /**
         * Enqueues a [OneTimeWorkRequest] ([CheckBirthdaysWorker]) to the [WorkManager].
         * The work is enqueued to be executed right away, only once.
         * There may be duplicates, and it will not conflict with [makePeriodicWorkRequest].
         * @param appContext application context
         * @return [OneTimeWorkRequest] enqueued work request (mainly for tests)
         */
        fun enqueueOneTimeWorkRequest(appContext: Context): OneTimeWorkRequest {
            val workRequest =
                OneTimeWorkRequestBuilder<CheckBirthdaysWorker>().build()
            WorkManager.getInstance(appContext).enqueue(workRequest)
            return workRequest
        }
    }

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val todayDayOfYear = today.dayOfYear
        val todayYear = today.year

        // Do not display notification several times in the same day.
        if (!checkLastTimeCheckedFromPreferences(today)) return Result.success()

        // Get the birthdays on today, and return directly if no hits
        val hits = withContext(Dispatchers.IO) { birthdayDao.findByDay(MonthDay.from(today)) }
        if (hits.isEmpty()) return Result.success()

        // Create the notification channel, and post notifications on it
        createNotificationChannel()
        val manager = NotificationManagerCompat.from(applicationContext)
        hits.forEachIndexed { idx, bDay ->
            // Integer id, useful to edit/remove the notification
            val notifId = getNotificationId(todayDayOfYear, idx)

            // Prepare notification's title/text
            val title = getString(R.string.notif_title, bDay.personName)
            var text = getString(
                R.string.notif_text,
                formatter.format(bDay.monthDay, bDay.year),
                bDay.personName
            )
            if (bDay.isYearKnown)
                text += getString(
                    R.string.notif_additional_text_year_known,
                    todayYear - bDay.year!!.value
                )

            // Build notif and display it
            val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_calendar_empty)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            manager.notify(notifId, notif)
        }
        return Result.success()
    }

    private fun checkLastTimeCheckedFromPreferences(today: LocalDate): Boolean {
        val sharedPreferences =
            applicationContext.getSharedPreferences(BACKGROUND_PREFERENCES, Context.MODE_PRIVATE)

        // if there was another check, and it was not before today, early exit
        val lastCheck = sharedPreferences.getString(LAST_BIRTHDAY_CHECK, null)?.let {
            LocalDate.parse(it)
        }
        // Update the LAST_BIRTHDAY_CHECK preference
        if (lastCheck != today) {
            sharedPreferences.edit().putString(LAST_BIRTHDAY_CHECK, today.toString()).apply()
        }

        // do the check if there was no preceding checks ; or if it was before
        return lastCheck == null || lastCheck.isBefore(today)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notif_channel_title)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)

            // Register the channel with the system
            val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
            )!!
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String =
        applicationContext.getString(id, *args)
}