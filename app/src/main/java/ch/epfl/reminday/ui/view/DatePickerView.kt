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

    val day: Int get() = calendar.get(Field.DAY_OF_MONTH)!!
    val month: Int get() = calendar.get(Field.MONTH)!!
    val year: Int? = calendar.get(Field.YEAR)

    init {
        addView(binding.root)

        val now = LocalDate.now()

        binding.apply {
            day.apply {
                minValue = calendar.getMinimum(Field.DAY_OF_MONTH)
                maxValue = calendar.getMaximum(Field.DAY_OF_MONTH)
                wrapSelectorWheel = false
                setOnValueChangedListener(valueChanged(Field.DAY_OF_MONTH))

                value = now.dayOfMonth
            }
            month.apply {
                minValue = calendar.getMinimum(Field.MONTH)
                maxValue = calendar.getMaximum(Field.MONTH)
                wrapSelectorWheel = false
                setOnValueChangedListener(valueChanged(Field.MONTH))

                month.displayedValues = (1..12).map {
                    monthFormatter.format(LocalDate.of(2000, it, 1))
                }.toTypedArray()
                value = now.monthValue
            }
            year.apply {
                minValue = calendar.getMinimum(Field.YEAR)
                maxValue = calendar.getMaximum(Field.YEAR)
                wrapSelectorWheel = false
                setOnValueChangedListener(valueChanged(Field.YEAR))

                value = now.year
            }
        }

        isYearEnabled = true
    }

    private fun valueChanged(field: Field) = NumberPicker.OnValueChangeListener { _, _, new ->
        calendar.set(field, new)
        binding.day.maxValue = calendar.getActualMaximum(Field.DAY_OF_MONTH)
    }
}
