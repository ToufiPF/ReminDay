package ch.epfl.reminday.data.contacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Event.CONTACT_ID
import android.provider.ContactsContract.CommonDataKinds.Event.START_DATE
import android.provider.ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ContactQueryTest {
    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: ContentResolver
    private lateinit var mockCursor: Cursor

    private lateinit var query: ContactQuery

    @Before
    fun init() {
        mockContext = mockk()
        mockContentResolver = mockk()
        mockCursor = mockk()

        every { mockContext.applicationContext } returns mockContext
        every { mockContext.contentResolver } returns mockContentResolver

        every {
            mockContentResolver.query(any(), any(), any(), any(), any())
        } returns mockCursor
        every {
            mockContentResolver.query(any(), any(), any(), any(), any(), any())
        } returns mockCursor

        query = ContactQuery(mockContext)
    }

    @Test
    fun queryCallsContentResolverQuery(): Unit = runBlocking {
        val uri: Uri = mockk()
        val projection = arrayOf(
            DISPLAY_NAME_PRIMARY,
            CONTACT_ID,
            START_DATE
        )
        val selection = "$DISPLAY_NAME_PRIMARY LIKE ?"
        val selectionArgs = arrayOf("Toufi")

        // Warning: SDK_INT isn't defined ->
        // ContentResolverCompat calls the variant without cancellationSignal,
        // and throws if cancellationSignal != null (because it can't be cancelled in SDKs < 16)
        val cursor = query.query(uri, projection, selection, selectionArgs, null, null)
        assertEquals(mockCursor, cursor)
    }
}