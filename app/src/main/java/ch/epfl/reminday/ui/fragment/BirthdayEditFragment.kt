package ch.epfl.reminday.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ch.epfl.reminday.R
import ch.epfl.reminday.databinding.FragmentBirthdayEditBinding
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import ch.epfl.reminday.viewmodel.fragment.BirthdayEditViewModel
import java.time.LocalDate

class BirthdayEditFragment : Fragment(R.layout.fragment_birthday_edit) {

    private val viewModel: BirthdayEditViewModel by viewModels()

    private var mBinding: FragmentBirthdayEditBinding? = null
    private val binding: FragmentBirthdayEditBinding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentBirthdayEditBinding.inflate(inflater, container, false)

        binding.apply {

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

                yearEnabled.value = true
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    private fun showDatePickerDialog() {
        val now = LocalDate.now()
        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.setField(Field.YEAR, year.toString())
                viewModel.setField(Field.MONTH, (month + 1).toString())
                viewModel.setField(Field.DAY_OF_MONTH, dayOfMonth.toString())
            },
            viewModel.getField(Field.YEAR) ?: now.year,
            (viewModel.getField(Field.MONTH) ?: now.monthValue) - 1,
            viewModel.getField(Field.DAY_OF_MONTH) ?: now.dayOfMonth,
        )
        picker.show()
    }

    private fun Editable.set(string: String?) {
        replace(0, length, string)
    }
}