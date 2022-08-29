package ch.epfl.reminday.data.security

import androidx.fragment.app.FragmentActivity


interface PromptUserUnlock {

    /**
     * Returns whether the device supports authentication (it has a password set up)
     */
    fun canAuthenticate(): Boolean

    /**
     * Prompts the user to authenticate. Returns true if successful.
     */
    suspend fun authenticate(activity: FragmentActivity): Boolean
}
