package ch.epfl.reminday.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.format.date.ShortFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CheckBirthdayService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    @Inject
    lateinit var birthdayDao: BirthdayDao

    @ShortFormat
    @Inject
    lateinit var formatter: DateFormatter

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val helper = CheckBirthdayNotifier(applicationContext, birthdayDao, formatter)
        scope.launch {
            helper.checkForBirthdayToday()

            stopSelfResult(startId)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
