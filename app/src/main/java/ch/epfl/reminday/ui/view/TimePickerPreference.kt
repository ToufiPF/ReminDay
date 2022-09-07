package ch.epfl.reminday.ui.view

import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ch.epfl.reminday.R
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

        private fun totalMinutesToHours(total: Int) = total / 60
        private fun totalMinutesToMinutes(total: Int) = total % 60
        private fun hoursMinutesToTotal(hours: Int, minutes: Int) = hours * 60 + minutes

        fun getActivity(context: Context): AppCompatActivity? {
            var c = context
            while (c is ContextWrapper) {
                if (c is AppCompatActivity) return c
                else c = c.baseContext
            }
            return null
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
    private var defaultTotal: Int = hoursMinutesToTotal(8, 0) // 08:00 (i.e. 8:00 AM)

    init {
        // Set the default layout if not specified
        if (attrs?.getAttributeValue(NAMESPACE_APP, "widgetLayout") == null &&
            attrs?.getAttributeValue(NAMESPACE_ANDROID, "widgetLayout") == null
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
            val time = LocalTime.parse(defaultValue as String, formatter)
            defaultTotal = hoursMinutesToTotal(time.hour, time.minute)
        }
    }

    private fun showPicker() {
        val total = sharedPreferences?.getInt(key, defaultTotal) ?: defaultTotal
        val hours = totalMinutesToHours(total)
        val minutes = totalMinutesToMinutes(total)

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hours)
            .setMinute(minutes)
            .setTitleText(title)
            .build()

        picker.addOnPositiveButtonClickListener {
            val newTotal = hoursMinutesToTotal(picker.hour, picker.minute)
            sharedPreferences?.edit()?.putInt(key, newTotal)?.apply()

            refresh()
        }

        val activity = getActivity(context)!!
        picker.show(activity.supportFragmentManager, this::class.simpleName)
    }

    private fun refresh() {
        val total = sharedPreferences?.getInt(key, defaultTotal) ?: defaultTotal
        val hours = totalMinutesToHours(total)
        val minutes = totalMinutesToMinutes(total)

        val time = LocalTime.of(hours, minutes)
        valueView.text =
            context.getString(R.string.prefs_notification_time_value, formatter.format(time))
    }
}
