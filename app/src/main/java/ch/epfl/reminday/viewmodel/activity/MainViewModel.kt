package ch.epfl.reminday.viewmodel.activity

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Event.*
import android.provider.ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
import android.provider.ContactsContract.Data.MIMETYPE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentResolverCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.ViewModel
import ch.epfl.reminday.data.birthday.Birthday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.MonthDay
import java.time.Year
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    companion object {
        const val READ_CONTACTS_PERMISSION_CODE = 0x100

        val birthdayFormats = arrayOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("--MM-dd"),
        )
    }

    fun mayRequireContacts(activity: AppCompatActivity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        if (checkSelfPermission(activity, READ_CONTACTS) == PERMISSION_GRANTED) return true

        if (activity.shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            println("Rationale")
        }
        activity.requestPermissions(arrayOf(READ_CONTACTS), READ_CONTACTS_PERMISSION_CODE)
        return false
    }

    suspend fun importContacts(context: Context): List<Birthday> {
        val uri = ContactsContract.Data.CONTENT_URI

        val projection: Array<out String> = arrayOf(
            DISPLAY_NAME_PRIMARY,
            CONTACT_ID,
            START_DATE,
        )

        val where = "$MIMETYPE = ? AND " +
                "$TYPE = $TYPE_BIRTHDAY"
        val selectionArgs: Array<out String> = arrayOf(
            CONTENT_ITEM_TYPE
        )

        val cursor = withContext(Dispatchers.IO) {
            ContentResolverCompat.query(
                context.contentResolver,
                uri,
                projection,
                where,
                selectionArgs,
                null,
                null
            )
        }

        val contacts = arrayListOf<Birthday>()
        cursor?.let {
            val nameColumn =
                cursor.getColumnIndex(DISPLAY_NAME_PRIMARY)
            val bDayColumn =
                cursor.getColumnIndex(START_DATE)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val dayString = cursor.getString(bDayColumn)

                val toAdd: Birthday = try {
                    val yearMonthDay = LocalDate.parse(dayString)
                    Birthday(
                        personName = name,
                        monthDay = MonthDay.of(yearMonthDay.month, yearMonthDay.dayOfMonth),
                        year = Year.of(yearMonthDay.year),
                    )
                } catch (e: DateTimeException) {
                    Birthday(
                        personName = name,
                        monthDay = MonthDay.parse(dayString),
                    )
                }

                contacts.add(toAdd)
            }
        }
        return contacts
    }
}