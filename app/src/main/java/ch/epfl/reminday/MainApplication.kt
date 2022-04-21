package ch.epfl.reminday

import android.util.Log
import androidx.multidex.MultiDexApplication
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import ch.epfl.reminday.background.BirthdayWorkerFactory
import ch.epfl.reminday.background.CheckBirthdayWorker
import ch.epfl.reminday.data.birthday.BirthdayDao
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : MultiDexApplication(), Configuration.Provider {

    @Inject
    lateinit var birthdayDao: BirthdayDao

    override fun getWorkManagerConfiguration(): Configuration {
        val factory = DelegatingWorkerFactory()
        factory.addFactory(BirthdayWorkerFactory(birthdayDao))

        return Configuration.Builder()
            .setWorkerFactory(factory)
            .setExecutor(Executors.newSingleThreadScheduledExecutor())
            .setMinimumLoggingLevel(Log.INFO)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        val workRequest = PeriodicWorkRequest.Builder(
            CheckBirthdayWorker::class.java,
            1,
            TimeUnit.DAYS,
            3,
            TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(CheckBirthdayWorker::class.java.name, REPLACE, workRequest)
    }
}
