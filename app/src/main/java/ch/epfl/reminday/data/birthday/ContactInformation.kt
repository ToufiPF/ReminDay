package ch.epfl.reminday.data.birthday

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Birthday::class,
            parentColumns = ["personName"],
            childColumns = ["personName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personName")]
)
data class ContactInformation(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val personName: String,
    val type: Type,
    val data: String,
) : Parcelable {

    enum class Type {
        PHONE,
        MESSENGER,
        WHATSAPP,
        OTHER;

        companion object {
            val ALL = listOf(*values())
        }
    }
}