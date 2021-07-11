package ch.epfl.reminday.data

import androidx.paging.PagingSource
import androidx.room.*
import java.time.MonthDay
import java.time.Year

/**
 * Data Access Object for birthdays.
 * Provides the methods that the app will use to interact with birthday data in DB.
 */
@Dao
interface BirthdayDao {

    @Query("SELECT * FROM birthday")
    fun getAll(): List<Birthday>

    @Query("SELECT * FROM birthday ORDER BY monthDay, year")
    fun getAllOrderedByMonthDayYear(): List<Birthday>

    @Query("SELECT * FROM birthday")
    fun pagingSource(): PagingSource<Int, Birthday>

    @Query("SELECT * FROM birthday WHERE personName LIKE :personName")
    fun findByName(personName: String): List<Birthday>

    @Query("SELECT * FROM birthday WHERE monthDay = :monthDay AND year = :year")
    fun findByDay(monthDay: MonthDay, year: Year): List<Birthday>

    @Query("SELECT * FROM birthday WHERE monthDay = :monthDay")
    fun findByDay(monthDay: MonthDay): List<Birthday>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg birthdays: Birthday)

    @Delete
    fun delete(birthday: Birthday)
}