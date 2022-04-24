package ch.epfl.reminday.background

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.format.date.DateFormatter
import java.util.*

/**
 * A subclass of [WorkerFactory] that can create [CheckBirthdaysWorker]s.
 */
class BirthdayWorkerFactory(
    private val birthdayDao: BirthdayDao,
    private val locale: Locale,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CheckBirthdaysWorker::class.java.name -> {
                val formatter = DateFormatter.shortFormatter(locale)
                CheckBirthdaysWorker(birthdayDao, formatter, appContext, workerParameters)
            }
            // Return null, so that the base class can delegate to the default WorkerFactory
            else ->
                null
        }
    }
}