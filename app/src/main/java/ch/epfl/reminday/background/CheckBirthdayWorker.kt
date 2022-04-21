package ch.epfl.reminday.background

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.epfl.reminday.data.birthday.BirthdayDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.MonthDay

class CheckBirthdayWorker(
    private val birthdayDao: BirthdayDao,
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = MonthDay.from(LocalDate.now())

        val hits = withContext(Dispatchers.IO) { birthdayDao.findByDay(today) }
        hits.forEach { birthday ->
            Toast.makeText(applicationContext, "Hello ${birthday.personName}", Toast.LENGTH_LONG).show()
        }
        return Result.success()
    }
}