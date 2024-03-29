package ch.epfl.reminday.util.constant

import android.content.SharedPreferences
import ch.epfl.reminday.background.CheckBirthdaysWorker

/**
 * Holds all the constants used for loading/storing in [SharedPreferences].
 */
object PreferenceNames {
    // =============================================================================================
    /**
     * Name of the [SharedPreferences] that stores general app options.
     * These preferences names are accessible via the android resources (R.string.prefs_***).
     */
    const val GENERAL_PREFERENCES = "general"

    // =============================================================================================
    /**
     * Name of the [SharedPreferences] that stores background services options.
     */
    const val BACKGROUND_PREFERENCES = "background"

    /** Preferences related to background services go here. */
    object BackgroundPreferenceNames {

        /**
         * Serialized date of the last check executed by [CheckBirthdaysWorker]
         */
        const val LAST_BIRTHDAY_CHECK = "last_day_check"
    }

    // =============================================================================================
    /**
     * Name of the [SharedPreferences] that stores encrypted values.
     */
    const val ENCRYPTED_PREFERENCES = "encrypted"

    /** Preferences that should be encrypted go here. */
    object EncryptedPreferenceNames {

        /**
         * Base64 string of the key used to encrypt the birthday database.
         * Can be null if database is not encrypted.
         */
        const val DATABASE_KEY = "db_key"
    }
}
