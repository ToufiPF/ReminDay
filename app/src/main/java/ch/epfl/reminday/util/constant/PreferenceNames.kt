package ch.epfl.reminday.util.constant

import ch.epfl.reminday.background.CheckBirthdaysWorker

object PreferenceNames {

    const val GENERAL_PREFERENCES = "general"
    object GeneralPreferenceNames {
        /**
         * Whether future delete confirmations should be skipped.
         * Defaults to false.
         */
        const val SKIP_DELETE_CONFIRMATION = "skip_delete_confirmation"
    }

    const val BACKGROUND_PREFERENCES = "background"
    object BackgroundPreferenceNames {

        /**
         * Serialized date of the last check executed by [CheckBirthdaysWorker]
         */
        const val LAST_BIRTHDAY_CHECK = "last_day_check"
    }
}