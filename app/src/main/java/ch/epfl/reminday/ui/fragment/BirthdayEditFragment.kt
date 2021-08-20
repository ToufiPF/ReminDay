package ch.epfl.reminday.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ch.epfl.reminday.R
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import ch.epfl.reminday.utils.Extensions.set
import ch.epfl.reminday.viewmodel.fragment.BirthdayEditViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BirthdayEditFragment : Fragment() {

    val viewModel: BirthdayEditViewModel by viewModels()

    private lateinit var yearEditText: EditText
    private lateinit var monthEditText: EditText
    private lateinit var dayEditText: EditText
    private lateinit var datePickerButton: Button

    @Inject
    lateinit var locale: Locale

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // chose whether we use the version with the days or months first
        val view = inflater.inflate(
            if (viewModel.isDayBeforeMonth(locale)) R.layout.fragment_birthday_edit_day_month_year
            else R.layout.fragment_birthday_edit_month_day_year,
            container,
            false
        )

        // the views are the same, only the organization changes
        yearEditText = view.findViewById(R.id.year_edit_text)
        monthEditText = view.findViewById(R.id.month_edit_text)
        dayEditText = view.findViewById(R.id.day_edit_text)
        datePickerButton = view.findViewById(R.id.date_picker_button)

        datePickerButton.setOnClickListener { showDatePickerDialog() }

        viewModel.apply {
            // let the edit texts update the viewModel
            yearEditText.addTextChangedListener { setField(Field.YEAR, it?.toString()) }
            monthEditText.addTextChangedListener { setField(Field.MONTH, it?.toString()) }
            dayEditText.addTextChangedListener { setField(Field.DAY_OF_MONTH, it?.toString()) }

            // observe the viewModel to keep edit texts updated
            yearEditContent.observe(viewLifecycleOwner) { yearEditText.text?.set(it) }
            monthEditContent.observe(viewLifecycleOwner) { monthEditText.text?.set(it) }
            dayEditContent.observe(viewLifecycleOwner) { dayEditText.text?.set(it) }

            yearEditText.text?.set(yearEditContent.value)
            monthEditText.text?.set(monthEditContent.value)
            dayEditText.text?.set(dayEditContent.value)

            yearEnabled.observe(viewLifecycleOwner) {
                yearEditText.visibility = if (it) View.VISIBLE else View.GONE
            }

            yearEnabled.value = false
        }

        return view
    }

    private fun showDatePickerDialog() {
        val now = LocalDate.now()
        val yearEnabled = viewModel.yearEnabled.value!!

        val initialYear =
            if (yearEnabled) viewModel.getField(Field.YEAR) ?: now.year
            else 1904

        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.setField(Field.YEAR, if (yearEnabled) year.toString() else null)
                viewModel.setField(Field.MONTH, (month + 1).toString())
                viewModel.setField(Field.DAY_OF_MONTH, dayOfMonth.toString())
            },
            initialYear,
            (viewModel.getField(Field.MONTH) ?: now.monthValue) - 1,
            viewModel.getField(Field.DAY_OF_MONTH) ?: now.dayOfMonth,
        )
        picker.show()

        picker.datePicker.apply {
            if (yearEnabled) {
                minDate = viewModel.minimumSupportedDateAsMillis()
                maxDate = viewModel.maximumSupportedDateAsMillis()
            } else {
                val yearId = resources.getIdentifier("year", "id", "android")
                val yearView: View? = findViewById(yearId)
                yearView?.visibility = View.GONE
            }
        }
    }
}