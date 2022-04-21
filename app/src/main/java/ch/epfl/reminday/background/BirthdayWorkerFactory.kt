package ch.epfl.reminday.background

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ch.epfl.reminday.data.birthday.BirthdayDao

class BirthdayWorkerFactory(
    private val birthdayDao: BirthdayDao
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            CheckBirthdayWorker::class.java.name ->
                CheckBirthdayWorker(birthdayDao, appContext, workerParameters)
            // Return null, so that the base class can delegate to the default WorkerFactory
            else ->
                null
        }
    }
}