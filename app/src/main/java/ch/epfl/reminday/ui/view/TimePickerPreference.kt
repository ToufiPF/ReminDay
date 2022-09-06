package ch.epfl.reminday.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ch.epfl.reminday.R
import ch.epfl.reminday.databinding.ViewTimePreferenceBinding
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
        private fun totalMinutesToHours(total: Int) = total / 60
        private fun totalMinutesToMinutes(total: Int) = total % 60
        private fun hoursMinutesToTotal(hours: Int, minutes: Int) = hours * 60 + minutes
    }

    // Can't use @AndroidEntryPoint because it needs to be a View/Fragment/Activity
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocaleEntryPoint {
        fun getLocale(): Locale
    }

    private lateinit var binding: ViewTimePreferenceBinding
    private val locale: Locale by lazy {
        val entryPoint = EntryPoints.get(context.applicationContext, LocaleEntryPoint::class.java)
//        val entryPoint = EntryPointAccessors.fromApplication(
//            context.applicationContext,
//            LocaleEntryPoint::class.java
//        )
        entryPoint.getLocale()
    }
    private val formatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("HH:mm", locale)
    }
    private var defaultTotal: Int = hoursMinutesToTotal(8, 0) // 08:00 (i.e. 8:00 AM)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = ViewTimePreferenceBinding.bind(holder.itemView)

        // Set title/summary fields
        binding.let {
            it.title.text = title
            it.summary.text = summary
        }

        refresh()

        binding.root.setOnClickListener {
            showPicker()
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

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
            .setTitleText(R.string.prefs_notification_time_title)
            .build()

        picker.addOnPositiveButtonClickListener {
            val newTotal = hoursMinutesToTotal(picker.hour, picker.minute)
            sharedPreferences?.edit()?.putInt(key, newTotal)?.apply()

            refresh()
        }

        val activity = context as AppCompatActivity
        picker.show(
            activity.supportFragmentManager, this::
            class.simpleName
        )
    }

    private fun refresh() {
        val total = sharedPreferences?.getInt(key, defaultTotal) ?: defaultTotal
        val hours = totalMinutesToHours(total)
        val minutes = totalMinutesToMinutes(total)

        val time = LocalTime.of(hours, minutes)
        binding.value.text =
            context.getString(R.string.prefs_notification_time_value, formatter.format(time))
    }
}
