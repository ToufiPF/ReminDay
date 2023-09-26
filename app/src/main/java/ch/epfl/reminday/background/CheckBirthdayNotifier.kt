package ch.epfl.reminday.background

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.util.constant.PreferenceNames.BACKGROUND_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.BackgroundPreferenceNames.LAST_BIRTHDAY_CHECK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.MonthDay

class CheckBirthdayNotifier(
    context: Context,
    private val birthdayDao: BirthdayDao,
    private val formatter: DateFormatter,
) {
    companion object {
        private const val CHANNEL_ID = "BirthdayNotificationsChannel"
    }

    private val appContext: Context = context.applicationContext

    suspend fun checkForBirthdayToday() {
        val today = LocalDate.now()
        val todayDayOfYear = today.dayOfYear
        val todayYear = today.year

        // Do not display notification several times in the same day.
        if (!checkLastTimeCheckedFromPreferences(today)) return

        // Get the birthdays on today, and return directly if no hits
        val hits = withContext(Dispatchers.IO) {
            birthdayDao.findByDay(MonthDay.from(today))
        }
        if (hits.isEmpty()) return

        // Create the notification channel, and post notifications on it
        createNotificationChannel()
        val manager = NotificationManagerCompat.from(appContext)
        hits.forEachIndexed { idx, bDay ->
            // Integer id, useful to edit/remove the notification
            val notifId = getNotificationId(todayDayOfYear, idx)

            // Prepare notification's title/text
            val title = appContext.getString(R.string.notif_title, bDay.personName)
            var text = appContext.getString(
                R.string.notif_text,
                formatter.format(bDay.monthDay, null),
                bDay.personName
            )
            if (bDay.isYearKnown)
                text += appContext.getString(
                    R.string.notif_additional_text_year_known,
                    todayYear - bDay.year!!.value
                )

            // Build notification and display it
            val notification = NotificationCompat.Builder(appContext, CHANNEL_ID).apply {
                setContentTitle(title)
                setContentText(text)
                setSmallIcon(R.drawable.ic_calendar_empty)
                priority = NotificationCompat.PRIORITY_DEFAULT
            }.build()

            manager.notify(notifId, notification)
        }
    }

    /** Returns true if the birthdays were not checked today yet */
    private fun checkLastTimeCheckedFromPreferences(today: LocalDate): Boolean {
        val prefs = appContext.getSharedPreferences(BACKGROUND_PREFERENCES, Context.MODE_PRIVATE)

        // if there was another check, and it was not before today, early exit
        val lastCheck = prefs.getString(LAST_BIRTHDAY_CHECK, null)?.let {
            LocalDate.parse(it)
        }
        // Update the LAST_BIRTHDAY_CHECK preference
        if (lastCheck != today) {
            prefs.edit().putString(LAST_BIRTHDAY_CHECK, today.toString()).apply()
        }

        // do the check if there was no preceding checks ; or if it was before
        return lastCheck == null || lastCheck.isBefore(today)
    }

    /** Creates the notification channel for the app (if it doesn't exist yet) */
    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_DEFAULT).apply {
            setName(appContext.getString(R.string.notif_channel_title))
            setDescription(appContext.getString(R.string.notif_channel_description))
        }.build()

        // Register the channel with the system
        NotificationManagerCompat.from(appContext).createNotificationChannel(channel)
    }

    /** Returns the notification id for a given birthday */
    private fun getNotificationId(dayOfYear: Int, idx: Int): Int =
        (idx shl 10) or dayOfYear
}
