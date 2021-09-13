package ch.epfl.reminday.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityBirthdayEditBinding
import ch.epfl.reminday.ui.activity.utils.BackArrowActivity
import ch.epfl.reminday.util.constant.ArgumentNames
import ch.epfl.reminday.util.Extensions.set
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year
import javax.inject.Inject

@AndroidEntryPoint
class BirthdayEditActivity : BackArrowActivity() {

    private lateinit var binding: ActivityBirthdayEditBinding

    @Inject
    lateinit var dao: BirthdayDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdayEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val birthday: Birthday? = intent.getParcelableExtra(ArgumentNames.BIRTHDAY)

        binding.apply {
            birthday?.let {
                nameEditText.text?.set(birthday.personName)
                birthdayEdit.year = birthday.year?.value
                birthdayEdit.month = birthday.monthDay.monthValue
                birthdayEdit.day = birthday.monthDay.dayOfMonth
            }

            nameEditText.addTextChangedListener { editable ->
                confirmButton.isEnabled = !editable?.toString().isNullOrBlank()
            }

            confirmButton.isEnabled = !nameEditText.text?.toString().isNullOrBlank()
            confirmButton.setOnClickListener { onConfirm() }
        }
    }

    private fun onConfirm() {
        val name = binding.nameEditText.text?.toString()
        val day = binding.birthdayEdit.day
        val month = binding.birthdayEdit.month
        val year = binding.birthdayEdit.year

        if (!name.isNullOrBlank()) {
            lifecycleScope.launch {
                val birthday = Birthday(name, MonthDay.of(month, day), year?.let { Year.of(it) })
                if (dao.findByName(name) != null) {
                    // a birthday with the same name exits, request confirmation
                    showConfirmOverwriteDialog {
                        lifecycleScope.launch { insertBirthdayAndFinish(birthday) }
                    }
                } else {
                    // fast path: insert & close activity
                    insertBirthdayAndFinish(birthday)
                }
            }
        }
    }

    private fun showConfirmOverwriteDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.birthday_will_be_overwritten)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                dialog.dismiss()
                onConfirm.invoke()
            }.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show()
    }

    private suspend fun insertBirthdayAndFinish(birthday: Birthday) {
        dao.insertAll(birthday)
        setResult(RESULT_OK)
        finish()
    }
}