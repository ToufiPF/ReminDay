package ch.epfl.reminday.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
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

    private fun openEncryptedPreferences(): SharedPreferences {
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

    @Suppress("SameParameterValue")
    private fun newRandom(bits: Int): ByteArray = ByteArray(bits / Byte.SIZE_BITS).apply {
        if (bits % Byte.SIZE_BITS != 0)
            throw IllegalArgumentException("Number of bits should be a multiple of ${Byte.SIZE_BITS}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            SecureRandom.getInstanceStrong().nextBytes(this)
        else
            SecureRandom().nextBytes(this)
    }

    override fun loadDatabaseKey(): ByteArray? {
        val preferences = openEncryptedPreferences()

        val base64Key = preferences.getString(DATABASE_KEY, null) ?: return null
        return Base64.decode(base64Key, Base64.DEFAULT)
    }

    override fun storeDatabaseKey(key: ByteArray?) {
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