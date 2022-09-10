package ch.epfl.reminday.ui.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ch.epfl.reminday.R
import ch.epfl.reminday.util.Extensions.getActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimePickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    companion object {
        private const val NAMESPACE_APP = "http://schemas.android.com/apk/res-auto"
        private const val NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
        private const val WIDGET_RES = "widgetLayout"

        private fun totalMinutesToHours(total: Int) = total / 60
        private fun totalMinutesToMinutes(total: Int) = total % 60
        private fun hoursMinutesToTotal(hours: Int, minutes: Int) = hours * 60 + minutes

        private val DEFAULT_TIME: LocalTime = LocalTime.of(8, 0)

        /**
         * Return the stored [LocalTime] corresponding to [key] in [pref]
         * @param pref [SharedPreferences] that holds the stored time
         * @param key [String] key corresponding to the time in the preferences
         * @param defaultTime [LocalTime] default value if [pref] is null/doesn't contain [key]
         * @return [LocalTime] the stored time corresponding to the given key
         */
        fun getStoredTime(
            pref: SharedPreferences?,
            key: String,
            defaultTime: LocalTime = DEFAULT_TIME
        ): LocalTime {
            val targetTotal = pref?.getInt(key, -1) ?: -1
            if (targetTotal < 0) return defaultTime

            val hours = totalMinutesToHours(targetTotal)
            val minutes = totalMinutesToMinutes(targetTotal)
            return LocalTime.of(hours, minutes)
        }
    }

    // Can't use @AndroidEntryPoint because it needs to be a View/Fragment/Activity
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocaleEntryPoint {
        fun getLocale(): Locale
    }

    private lateinit var valueView: TextView
    private val locale: Locale by lazy {
        val entryPoint = EntryPoints.get(context.applicationContext, LocaleEntryPoint::class.java)
        entryPoint.getLocale()
    }
    private val formatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("HH:mm", locale)
    }
    private var defaultTime: LocalTime = LocalTime.of(8, 0) // 08:00 (i.e. 8:00 AM)

    init {
        // Set the default layout if not specified
        if (attrs?.getAttributeValue(NAMESPACE_APP, WIDGET_RES) == null &&
            attrs?.getAttributeValue(NAMESPACE_ANDROID, WIDGET_RES) == null
        ) {
            Log.i(this::class.simpleName, "Setting default widget layout")
            widgetLayoutResource = R.layout.widget_time_preference
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        valueView = holder.itemView.findViewById(R.id.value)

        // Set the time in the value field:
        refresh()

        holder.itemView.setOnClickListener {
            showPicker()
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? = a.getString(index)
    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue != null) {
            defaultTime = LocalTime.parse(defaultValue as String, formatter)
        }
    }

    private fun showPicker() {
        val targetTime = getStoredTime(sharedPreferences, key, defaultTime)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(targetTime.hour)
            .setMinute(targetTime.minute)
            .setTitleText(title)
            .build()

        picker.addOnPositiveButtonClickListener {
            val newTotal = hoursMinutesToTotal(picker.hour, picker.minute)
            sharedPreferences?.edit()?.putInt(key, newTotal)?.apply()

            refresh()
        }

        val activity = context.getActivity()!!
        picker.show(activity.supportFragmentManager, this::class.simpleName)
    }

    private fun refresh() {
        val time = getStoredTime(sharedPreferences, key)
        valueView.text =
            context.getString(R.string.prefs_notification_time_value, formatter.format(time))
    }
}
