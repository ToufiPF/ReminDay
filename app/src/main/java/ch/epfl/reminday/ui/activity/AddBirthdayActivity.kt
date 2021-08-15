package ch.epfl.reminday.ui.activity

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
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
    }

    private fun onDatePickerIconClicked() {
        // hide keyboard
        val inputManager = getSystemService(this, InputMethodManager::class.java)
        inputManager?.hideSoftInputFromWindow(binding.root.windowToken, 0)

        // show the DatePicker
    }
}