package ch.epfl.reminday.data.contacts

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wrapper to perform queries on contacts via ContentResolverCompat ;
 * can be mocked for tests.
 */
open class ContactQuery(
    appContext: Context
) {
    private val contentResolver = appContext.contentResolver

    open suspend fun query(
        uri: Uri,
        projection: Array<out String>,
        selection: String,
        selectionArgs: Array<out String>,
        order: String?,
        cancellationSignal: CancellationSignal?
    ): Cursor? = withContext(Dispatchers.IO) {
        ContentResolverCompat.query(
            contentResolver,
            uri,
            projection,
            selection,
            selectionArgs,
            order,
            cancellationSignal
        )
    }
}