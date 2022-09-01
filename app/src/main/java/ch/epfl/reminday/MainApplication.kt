package ch.epfl.reminday

import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import ch.epfl.reminday.background.BirthdayWorkerFactory
import ch.epfl.reminday.background.CheckBirthdaysWorker
import ch.epfl.reminday.background.CheckBirthdaysWorker.Companion.enqueueOneTimeWorkRequest
import ch.epfl.reminday.background.CheckBirthdaysWorker.Companion.enqueuePeriodicWorkRequest
import ch.epfl.reminday.data.birthday.BirthdayDao
import dagger.hilt.android.HiltAndroidApp
import java.util.*
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : MultiDexApplication(), Configuration.Provider {

    companion object {
        /**
         * Builds a custom WorkManager [Configuration.Builder] with a factory
         * that can create [CheckBirthdaysWorker].
         * @param birthdayDao [BirthdayDao] required by [BirthdayWorkerFactory]
         * @param locale [Locale] required by [BirthdayWorkerFactory]
         * @return [Configuration.Builder] with a factory and a few options already set (eg. log level)
         */
        fun makeWorkManagerConfigurationBuilder(
            birthdayDao: BirthdayDao,
            locale: Locale,
        ): Configuration.Builder {
            val factory = BirthdayWorkerFactory(birthdayDao, locale)

            return Configuration.Builder()
                .setWorkerFactory(factory)
                .setMinimumLoggingLevel(Log.INFO)
        }
    }

    @Inject
    lateinit var locale: Locale

    @Inject
    lateinit var birthdayDao: BirthdayDao

    override fun getWorkManagerConfiguration(): Configuration =
        makeWorkManagerConfigurationBuilder(birthdayDao, locale).build()

    override fun onCreate() {
        super.onCreate()

        // If not already done (should have been at device startup)
        // enqueue a periodic worker that checks if each day is someone's birthday
        enqueuePeriodicWorkRequest(applicationContext)

        // Also do the check once when the app starts
        enqueueOneTimeWorkRequest(applicationContext)
    }
}
