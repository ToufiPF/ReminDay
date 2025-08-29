package ch.epfl.reminday.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Registered to receive BOOT_COMPLETED signal from Android.
 * cf. AndroidManifest.xml
 */
class BootBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED"
    }

    /**
     * Called when the phone has completely booted.
     * Calls [CheckBirthdaysWorker.enqueueOneTimeWorkRequest] and
     * [CheckBirthdaysWorker.enqueuePeriodicWorkRequest].
     * @param context application context
     * @param intent the intent that triggered [BroadcastReceiver.onReceive]
     * @see BroadcastReceiver
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != BOOT_COMPLETED_ACTION) return

        context?.let {
            AlarmBroadcastReceiver.enqueuePeriodicAlarmRequest(context)
            AlarmBroadcastReceiver.enqueueOneTimeAlarmRequest(context)
        }
    }
}
