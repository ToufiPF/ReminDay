package ch.epfl.reminday.util

import android.content.SharedPreferences
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