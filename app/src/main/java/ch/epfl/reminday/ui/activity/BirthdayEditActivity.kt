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
import ch.epfl.reminday.util.Extensions.set
import ch.epfl.reminday.util.constant.ArgumentNames
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.MonthDay
import java.time.Year
import javax.inject.Inject

@AndroidEntryPoint
class BirthdayEditActivity : BackArrowActivity() {

    enum class Mode {
        /**
         * In this mode, [BirthdayEditActivity] will only add the input [Birthday].
         * If a birthday has the same name that the one input by the user,
         * shows a confirmation dialog before overwriting it.
         */
        ADD,

        /**
         * In this mode, [BirthdayEditActivity] will replace the [Birthday] passed
         * to the activity via [ArgumentNames.BIRTHDAY] by the one input by the user.
         * If another birthday than the one edited has the same name that the one input by the user,
         * shows a confirmation dialog before overwriting it.
         */
        EDIT;

        companion object {
            val ALL = listOf(*values())

            const val DEFAULT_ORDINAL = 0
        }
    }

    private lateinit var binding: ActivityBirthdayEditBinding
    private var birthday: Birthday? = null
    private lateinit var mode: Mode

    @Inject
    lateinit var dao: BirthdayDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdayEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        birthday = intent.getParcelableExtra(ArgumentNames.BIRTHDAY)
        mode = Mode.ALL[intent.getIntExtra(
            ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL,
            Mode.DEFAULT_ORDINAL
        )]

        binding.apply {
            birthday?.let { birthday ->
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
                val b = Birthday(name, MonthDay.of(month, day), year?.let { Year.of(it) })

                // if we're in Edit mode, delete the previous birthday
                when (mode) {
                    Mode.ADD -> {
                        val existing = dao.findByName(name)
                        if (existing != null)
                            showConfirmOverwriteDialog {
                                // a birthday with the same name exists, request confirmation to overwrite
                                lifecycleScope.launch {
                                    insertBirthdayAndFinish(b)
                                }
                            }
                        else {
                            // fast path: insert & close activity
                            insertBirthdayAndFinish(b)
                        }
                    }
                    Mode.EDIT -> {
                        if (birthday?.personName != name && dao.findByName(name) != null)
                            showConfirmOverwriteDialog {
                                lifecycleScope.launch {
                                    birthday?.let { dao.delete(it) }
                                    insertBirthdayAndFinish(b)
                                }
                            }
                        else {
                            birthday?.let { dao.delete(it) }
                            insertBirthdayAndFinish(b)
                        }
                    }
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