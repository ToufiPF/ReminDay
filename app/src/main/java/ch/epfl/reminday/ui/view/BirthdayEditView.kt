package ch.epfl.reminday.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.widget.addTextChangedListener
import ch.epfl.reminday.databinding.ViewBirthdayEditBinding
import ch.epfl.reminday.format.calendar.MyGregorianCalendar
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field

class BirthdayEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewBirthdayEditBinding =
        ViewBirthdayEditBinding.inflate(LayoutInflater.from(context))

    var enableYear: Boolean = true
        set(value) {
            field = value
            binding.yearEditText.visibility = if (enableYear) View.VISIBLE else View.GONE
        }

    private val calendar = MyGregorianCalendar()
    private var isYearBeingTyped: Boolean = false

    init {
        addView(binding.root)

        binding.apply {
            dayEditText.addTextChangedListener(afterTextChanged = onDateTextChanged(Field.DAY_OF_MONTH))
            monthEditText.addTextChangedListener(afterTextChanged = onDateTextChanged(Field.MONTH))
            yearEditText.addTextChangedListener {
                // allow user to write years < 1800
                isYearBeingTyped = it != null && it.length < 4

                val value = it?.toString()?.toIntOrNull()
                calendar.setNullable(Field.YEAR, value)
                refreshEditTexts()
            }

            datePickerButton.setOnClickListener { showDatePickerDialog() }
        }

        enableYear = true
        refreshEditTexts()
    }

    private fun showDatePickerDialog() {

    }

    private fun onDateTextChanged(field: Field): (Editable?) -> Unit = {
        val value = it?.toString()?.toIntOrNull()
        calendar.setNullable(field, value)
        refreshEditTexts()
    }

    private fun refreshEditTexts() {
        binding.apply {
            refreshEditText(dayEditText.text, Field.DAY_OF_MONTH)
            refreshEditText(monthEditText.text, Field.MONTH)
            if (!isYearBeingTyped) refreshEditText(yearEditText.text, Field.YEAR)
        }
    }

    private fun refreshEditText(edit: Editable?, field: Field) {
        val str = calendar.get(field)?.toString() ?: ""
        if (edit?.toString() != str)
            edit?.replace(0, edit.length, str)
    }
}