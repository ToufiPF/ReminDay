package ch.epfl.reminday.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.findFragment
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityAddBirthdayBinding
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import ch.epfl.reminday.ui.fragment.BirthdayEditFragment
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

    private val birthdayEdit: BirthdayEditFragment
        get() = binding.birthdayEdit[0].findFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirm.setOnClickListener { onConfirm() }
    }

    private fun onConfirm() {
        val name = binding.nameEditText.text?.toString()
        val day = birthdayEdit.viewModel.getField(Field.DAY_OF_MONTH)
        val month = birthdayEdit.viewModel.getField(Field.MONTH)
        val year = birthdayEdit.viewModel.getField(Field.YEAR)

        if (name != null && day != null && month != null) {
            lifecycleScope.launch {
                val birthday = Birthday(name, MonthDay.of(month, day), year?.let { Year.of(it) })
                dao.insertAll(birthday)
                finish()
            }
        }
    }
}