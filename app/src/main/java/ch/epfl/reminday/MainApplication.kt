package ch.epfl.reminday

import androidx.multidex.MultiDexApplication
import ch.epfl.reminday.background.AlarmBroadcastReceiver.Companion.enqueueOneTimeAlarmRequest
import ch.epfl.reminday.background.AlarmBroadcastReceiver.Companion.enqueuePeriodicAlarmRequest
import ch.epfl.reminday.data.birthday.BirthdayDao
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : MultiDexApplication() {
    
    @Inject
    lateinit var locale: Locale

    @Inject
    lateinit var birthdayDao: BirthdayDao

    override fun onCreate() {
        super.onCreate()

        // If not already done (should have been at device startup)
        // enqueue a periodic worker that checks if each day is someone's birthday
        enqueuePeriodicAlarmRequest(applicationContext)

        // Also do the check once when the app starts
        enqueueOneTimeAlarmRequest(applicationContext)
    }
}
