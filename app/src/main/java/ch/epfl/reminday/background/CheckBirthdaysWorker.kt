package ch.epfl.reminday.background

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.ui.view.TimePickerPreference
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class CheckBirthdaysWorker(
    private val birthdayDao: BirthdayDao,
    private val formatter: DateFormatter,
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private fun makePeriodicWorkRequest(context: Context): PeriodicWorkRequest {
            val targetTime = TimePickerPreference.getStoredTime(
                context.getSharedPreferences(GENERAL_PREFERENCES, MODE_PRIVATE),
                context.getString(R.string.prefs_notification_time)
            )

            val now = LocalDateTime.now()
            val targetDateTime = now.withHour(targetTime.hour).withMinute(targetTime.minute)
                .truncatedTo(ChronoUnit.HOURS)
            val future =
                if (now.isBefore(targetDateTime)) targetDateTime
                else targetDateTime.plusDays(1)

            // set initial delay to do the check on the next target time
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
            val workRequest = makePeriodicWorkRequest(appContext)
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
        val helper = CheckBirthdayNotifier(applicationContext, birthdayDao, formatter)
        helper.checkForBirthdayToday()
        return Result.success()
    }
}
