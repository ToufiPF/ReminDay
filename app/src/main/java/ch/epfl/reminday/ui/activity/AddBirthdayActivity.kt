package ch.epfl.reminday.ui.activity

import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import ch.epfl.reminday.databinding.ActivityAddBirthdayBinding
import ch.epfl.reminday.viewmodel.activity.AddBirthdayViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBirthdayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBirthdayBinding

    private val viewModel: AddBirthdayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.birthdayEditLayout.setEndIconOnClickListener {
            onDatePickerIconClicked()
        }

        binding.birthdayEditText.apply {
            inputType = InputType.TYPE_DATETIME_VARIATION_DATE

            addTextChangedListener { editable ->
                val string = editable?.toString() ?: return@addTextChangedListener
                if (!viewModel.isInputDateStringValid(string)) {
                    editable.clear()
                    editable.append(viewModel.correctInputDateString(string))
                }
            }
        }
        viewModel.hintPattern.observe(this) { pattern ->
            binding.birthdayEditText.hint = pattern
        }
    }

    private fun onDatePickerIconClicked() {
        // hide keyboard
        val inputManager = getSystemService(this, InputMethodManager::class.java)
        inputManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)

        // give focus to the birthday edit text
        binding.birthdayEditText.requestFocus()

        // show the DatePicker
    }
}