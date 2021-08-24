package ch.epfl.reminday.data.birthday

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

    /**
     * Returns a paging source with all birthdays, ordered by primary key (ie. [Birthday.personName])
     * @return [PagingSource] with all birthdays
     */
    @Query("SELECT * FROM birthday")
    fun pagingSource(): PagingSource<Int, Birthday>

    /**
     * Returns a paging source with all birthdays, ordered by (month, year, name)
     * @return [PagingSource] with all birthdays
     */
    @Query("SELECT * FROM birthday ORDER BY monthDay, year")
    fun pagingSourceOrderedByMonthDayYear(): PagingSource<Int, Birthday>


    @Query("SELECT * FROM birthday")
    suspend fun getAll(): List<Birthday>

    @Query("SELECT * FROM birthday ORDER BY monthDay, year")
    suspend fun getAllOrderedByMonthDayYear(): List<Birthday>


    @Query("SELECT * FROM birthday WHERE personName LIKE :personName")
    suspend fun findByName(personName: String): Birthday

    @Query("SELECT * FROM birthday WHERE monthDay = :monthDay")
    suspend fun findByDay(monthDay: MonthDay): List<Birthday>

    @Query("SELECT * FROM birthday WHERE monthDay = :monthDay AND year = :year")
    suspend fun findByDay(monthDay: MonthDay, year: Year): List<Birthday>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg birthdays: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)
}