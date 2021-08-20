package ch.epfl.reminday.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityAddBirthdayBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year
import javax.inject.Inject

@AndroidEntryPoint
class AddBirthdayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBirthdayBinding

    @Inject
    lateinit var dao: BirthdayDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirm.setOnClickListener { onConfirm() }
    }

    private fun onConfirm() {
        val name = binding.nameEditText.text?.toString()
        val day = binding.birthdayEdit.day
        val month = binding.birthdayEdit.month
        val year = binding.birthdayEdit.year

        if (name != null) {
            lifecycleScope.launch {
                val birthday = Birthday(name, MonthDay.of(month, day), year?.let { Year.of(it) })
                dao.insertAll(birthday)
                finish()
            }
        }
    }
}