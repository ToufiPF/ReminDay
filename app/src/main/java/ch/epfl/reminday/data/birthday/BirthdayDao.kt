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

    companion object {
        private const val allUnordered =
            "SELECT * FROM birthday"

        private const val allByMonthDayYear =
            "SELECT * FROM birthday ORDER BY monthDay, year"

        private const val allByMonthDayYearFromToday =
            "SELECT *, 1 as rowOrder FROM birthday WHERE monthDay >= :today " +
                    "UNION ALL SELECT *, 2 as rowOrder FROM birthday WHERE monthDay < :today " +
                    "ORDER BY rowOrder, monthDay, year"
    }

    /**
     * Returns a paging source with all birthdays, ordered by primary key (ie. [Birthday.personName])
     * @return [PagingSource] with all birthdays
     */
    @Query(allUnordered)
    fun pagingSource(): PagingSource<Int, Birthday>

    @Query(allUnordered)
    suspend fun getAll(): List<Birthday>


    /**
     * Returns a paging source with all birthdays, ordered by (month, year, name)
     * @return [PagingSource] with all birthdays
     */
    @Query(allByMonthDayYear)
    fun pagingSourceOrderedByMonthDayYear(): PagingSource<Int, Birthday>

    @Query(allByMonthDayYear)
    suspend fun getAllOrderedByMonthDayYear(): List<Birthday>

    // false positive: rowOrder is used in ORDER BY
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(allByMonthDayYearFromToday)
    fun pagingSourceOrderedByMonthDayYearFrom(today: MonthDay): PagingSource<Int, Birthday>

    // false positive: rowOrder is used in ORDER BY
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query(allByMonthDayYearFromToday)
    suspend fun getAllOrderedByMonthDayYearFrom(today: MonthDay): List<Birthday>


    @Query("SELECT * FROM birthday WHERE personName LIKE :personName")
    suspend fun findByName(personName: String): Birthday?

    @Query("SELECT * FROM birthday WHERE monthDay = :monthDay")
    suspend fun findByDay(monthDay: MonthDay): List<Birthday>

    @Query("SELECT * FROM birthday WHERE monthDay = :monthDay AND year = :year")
    suspend fun findByDay(monthDay: MonthDay, year: Year): List<Birthday>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg birthdays: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)
}