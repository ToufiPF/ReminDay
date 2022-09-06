package ch.epfl.reminday.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import ch.epfl.reminday.util.constant.PreferenceNames.ENCRYPTED_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.EncryptedPreferenceNames.DATABASE_KEY
import java.security.SecureRandom

class DatabaseKeyManagerImpl(
    private val appContext: Context
) : DatabaseKeyManager {

    @Suppress("SameParameterValue")
    private fun newRandom(bits: Int): ByteArray = ByteArray(bits / Byte.SIZE_BITS).also {
        if (bits % Byte.SIZE_BITS != 0)
            throw IllegalArgumentException("Number of bits should be a multiple of ${Byte.SIZE_BITS}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            SecureRandom.getInstanceStrong().nextBytes(it)
        else
            SecureRandom().nextBytes(it)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun openEncryptedPreferences(): SharedPreferences {
        val mainKey = MasterKey.Builder(appContext)
            .setUserAuthenticationRequired(false)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            appContext,
            ENCRYPTED_PREFERENCES,
            mainKey,
            PrefKeyEncryptionScheme.AES256_SIV,
            PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // TODO: androidx.security.crypto requires API >= 23 for now.
    //  Once 1.1.0 is out, check if API >= 21 is possible.
    // FIXME: Seems crashes are introduced by the DB encryption
    override fun isDatabaseEncryptionSupported(): Boolean = false
//        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    override fun loadDatabaseKey(): ByteArray? {
        if (!isDatabaseEncryptionSupported())
            throw IllegalStateException("Cannot load key from encrypted database: encryption not supported in this Android version.")

        // Return the stored key (if any)
        val preferences = openEncryptedPreferences()
        val base64Key = preferences.getString(DATABASE_KEY, null)
        if (base64Key != null)
            return Base64.decode(base64Key, Base64.DEFAULT)

        // TODO: add option: may not want to generate a new key and simply store the db in clear
        // No key was set up yet: generate a new one
        Log.i(this::class.simpleName, "Generating new key.")
        val newKey = newDatabaseKey()
        storeDatabaseKey(newKey)
        return newKey
    }

    override fun storeDatabaseKey(key: ByteArray?) {
        if (!isDatabaseEncryptionSupported())
            throw IllegalStateException("Cannot store key to encrypted database: encryption not supported in this Android version.")

        val preferences = openEncryptedPreferences()
        preferences.edit().apply {
            if (key == null) {
                remove(DATABASE_KEY)
            } else {
                val base64Key = Base64.encodeToString(key, Base64.DEFAULT)
                putString(DATABASE_KEY, base64Key)
            }

            apply()
        }
    }

    override fun newDatabaseKey(): ByteArray = newRandom(256)
}