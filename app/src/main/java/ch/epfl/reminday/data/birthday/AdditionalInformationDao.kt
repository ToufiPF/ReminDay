package ch.epfl.reminday.data.birthday

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AdditionalInformationDao {

    @Query("SELECT * FROM AdditionalInformation")
    suspend fun getAll(): List<AdditionalInformation>

    @Query("SELECT * FROM AdditionalInformation WHERE personName LIKE :personName")
    suspend fun getInfoForName(personName: String): List<AdditionalInformation>

    @Insert
    suspend fun insertAll(vararg info: AdditionalInformation)

    @Delete
    suspend fun delete(vararg info: AdditionalInformation)
}
