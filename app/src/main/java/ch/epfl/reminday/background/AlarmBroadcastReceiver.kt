package ch.epfl.reminday.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.format.date.ShortFormat
import ch.epfl.reminday.util.Extensions.goAsync
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlarmBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CheckBirthdayNotifier"
        private const val ACTION = "ch.epfl.reminday.action.DAILY_BIRTHDAY_CHECK"
        private const val REQUEST_CODE = 1234

        @VisibleForTesting
        fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

        fun enqueuePeriodicAlarmRequest(appContext: Context) {
            val manager = ContextCompat.getSystemService(appContext, AlarmManager::class.java)
            if (manager == null) {
                Log.e(TAG, "getSystemService(AlarmManager) returned null")
                return
            }

            val intent = Intent(appContext, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION
            val pending = PendingIntent.getBroadcast(
                appContext,
                REQUEST_CODE,
                intent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )

            val checkTime = CheckBirthdayNotifier.getInitialCheckTime(appContext)
            val millis = LocalDateTime.now().until(checkTime, ChronoUnit.MILLIS)
            manager.setInexactRepeating(
                AlarmManager.RTC,
                getCurrentTimeMillis() + millis,
                AlarmManager.INTERVAL_HALF_DAY,
                pending,
            )
        }

        fun enqueueOneTimeAlarmRequest(appContext: Context) {
            val intent = Intent(appContext, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION
            appContext.sendBroadcast(intent)
        }
    }

    @Inject
    lateinit var birthdayDao: BirthdayDao

    @Inject
    @ShortFormat
    lateinit var formatter: DateFormatter

    override fun onReceive(context: Context?, intent: Intent?) = goAsync {
        if (context == null || intent?.action != ACTION) return@goAsync
        Log.i(TAG, "Received intent with correct action")

        val helper = CheckBirthdayNotifier(context.applicationContext, birthdayDao, formatter)
        helper.checkForBirthdayToday()
    }
}
