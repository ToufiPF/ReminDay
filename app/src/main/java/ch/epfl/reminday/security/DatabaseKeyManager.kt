package ch.epfl.reminday.security

import androidx.security.crypto.EncryptedSharedPreferences

/**
 * Interface that load/stores the database keys from Android's [EncryptedSharedPreferences].
 */
interface DatabaseKeyManager {

    /**
     * Returns the database key, or null if the database is stored in clear.
     */
    fun loadDatabaseKey(): ByteArray?

    /**
     * Stores the given database key. Pass null if you want to store the database in clear.
     */
    fun storeDatabaseKey(key: ByteArray?)

    /**
     * Generates and returns a new random 256bit key.
     */
    fun newDatabaseKey(): ByteArray
}
