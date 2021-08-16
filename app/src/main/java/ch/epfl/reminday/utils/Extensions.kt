package ch.epfl.reminday.utils

import android.text.Editable

/**
 * Extension functions for various classes.
 */
object Extensions {

    /**
     * Sets the whole [Editable]'s text to the given [string].
     * @param string the [String] to write into the [Editable]
     */
    fun Editable.set(string: String?) {
        replace(0, length, string ?: "")
    }
}