package ch.epfl.reminday.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import ch.epfl.reminday.databinding.ViewDatePickerBinding
import ch.epfl.reminday.format.calendar.MyGregorianCalendar
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewDatePickerBinding =
        ViewDatePickerBinding.inflate(LayoutInflater.from(context))

    @Inject
    lateinit var locale: Locale

    private val monthFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM", locale)

    private val calendar = MyGregorianCalendar()

    /**
     * Dictates whether the [DatePickerView] will show the field to chose the year.
     */
    var isYearEnabled: Boolean = true
        set(value) {
            field = value

            val now = LocalDate.now()
            binding.year.visibility = if (value) View.VISIBLE else View.GONE
            calendar.setNullable(Field.YEAR, if (value) now.year else null)
            binding.day.maxValue = calendar.getActualMaximum(Field.DAY_OF_MONTH)
        }

    var day: Int
        get() = calendar.get(Field.DAY_OF_MONTH)!!
        set(value) {
            binding.day.value = value
            calendar.set(Field.DAY_OF_MONTH, value)
        }
    var month: Int
        get() = calendar.get(Field.MONTH)!!
        set(value) {
            binding.month.value = value
            calendar.set(Field.MONTH, value)
        }
    var year: Int?
        get() = calendar.get(Field.YEAR)
        set(value) {
            if (value == null) isYearEnabled = false
            else {
                isYearEnabled = true
                binding.year.value = value
            }
            calendar.setNullable(Field.YEAR, value)
        }

    init {
        addView(binding.root)

        val now = LocalDate.now()

        binding.apply {
            initNumberPicker(year, Field.YEAR, now.year)
            initNumberPicker(month, Field.MONTH, now.monthValue)
            initNumberPicker(day, Field.DAY_OF_MONTH, now.dayOfMonth)

            month.displayedValues = (1..12).map {
                monthFormatter.format(LocalDate.of(2000, it, 1))
            }.toTypedArray()
        }

        isYearEnabled = true
    }

    private fun initNumberPicker(picker: NumberPicker, field: Field, initialValue: Int) {
        picker.apply {
            minValue = calendar.getMinimum(field)
            maxValue = calendar.getActualMaximum(field)
            wrapSelectorWheel = false

            setOnValueChangedListener { _, _, newVal ->
                calendar.set(field, newVal)
                binding.day.maxValue = calendar.getActualMaximum(Field.DAY_OF_MONTH)
            }
            value = initialValue
            calendar.set(field, value)
        }
    }
}
