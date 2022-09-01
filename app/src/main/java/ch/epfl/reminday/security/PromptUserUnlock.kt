package ch.epfl.reminday.security

import androidx.fragment.app.FragmentActivity

/**
 * Interface to handle user authentication (via biometrics/password).
 */
interface PromptUserUnlock {

    /**
     * Returns whether the device supports authentication.
     * @return true if authentication is supported (i.e.
     */
    fun canAuthenticate(): Boolean

    /**
     * Prompts the user to authenticate.
     * @param activity [FragmentActivity] the prompt should be drawn upon
     * @return true if successful, false if user failed to authenticate/cancelled the prompt.
     */
    suspend fun authenticate(activity: FragmentActivity): Boolean
}
