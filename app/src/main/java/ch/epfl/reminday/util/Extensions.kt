package ch.epfl.reminday.util

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.reminday.R
import ch.epfl.reminday.databinding.DialogDoNotAskAgainBinding

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

    /**
     * Returns the [Parcelable] extra corresponding to [key] in the [Intent].
     * @param key [String] the parcel key
     * @return [Parcelable] the parcel corresponding to [key]
     */
    inline fun <reified T> Intent.parcelable(key: String): T? = when {
        SDK_INT >= TIRAMISU -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    /**
     * Returns the [Parcelable] extra corresponding to [key] in the [Bundle].
     * @param key [String] the parcel key
     * @return [Parcelable] the parcel corresponding to [key]
     */
    inline fun <reified T> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= TIRAMISU -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    /**
     * Returns the [AppCompatActivity] tied to the [Context] (if any).
     * @return [AppCompatActivity], or null if failed to get the activity.
     */
    fun Context.getActivity(): AppCompatActivity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) return context
            else context = context.baseContext
        }
        return null
    }

    /**
     * Shows a default confirmation dialog with a custom title and message,
     * and "confirm" and "cancel" buttons.
     * @param title Int, R.string of the title to print
     * @param text Int, R.string of the message to print
     * @param onConfirm will be run only if the user presses "confirm".
     */
    fun AppCompatActivity.showConfirmationDialog(
        @StringRes title: Int,
        @StringRes text: Int,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                dialog.dismiss()
                onConfirm.invoke()
            }.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    fun AppCompatActivity.showConfirmationDialogWithDoNotAskAgain(
        @StringRes title: Int,
        @StringRes text: Int,
        preferences: SharedPreferences,
        showConfirmationFlag: String,
        onConfirm: () -> Unit
    ) {
        val showConfirmation = preferences.getBoolean(showConfirmationFlag, true)
        if (showConfirmation) {
            val binding = DialogDoNotAskAgainBinding.inflate(layoutInflater)
            binding.messageTextView.text = getString(text)

            AlertDialog.Builder(this)
                .setTitle(title)
                .setView(binding.root)
                .setPositiveButton(R.string.confirm) { dialog, _ ->
                    dialog.dismiss()
                    preferences.edit()
                        .putBoolean(showConfirmationFlag, !binding.checkboxDoNotAskAgain.isChecked)
                        .apply()
                    onConfirm.invoke()
                }.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.show()
        } else {
            onConfirm.invoke()
        }
    }
}