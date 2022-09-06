package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.idling.CountingIdlingResource
import ch.epfl.reminday.R
import ch.epfl.reminday.adapter.BirthdayEditAddInfoAdapter
import ch.epfl.reminday.adapter.BirthdayEditInfoAdapter
import ch.epfl.reminday.data.birthday.AdditionalInformation
import ch.epfl.reminday.data.birthday.AdditionalInformationDao
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityBirthdayEditBinding
import ch.epfl.reminday.ui.activity.utils.BackArrowActivity
import ch.epfl.reminday.util.Extensions.parcelable
import ch.epfl.reminday.util.Extensions.set
import ch.epfl.reminday.util.Extensions.showConfirmationDialog
import ch.epfl.reminday.util.constant.ArgumentNames
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY
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

    private lateinit var addInfoAdapter: BirthdayEditAddInfoAdapter
    private lateinit var infoAdapter: BirthdayEditInfoAdapter

    @Inject
    lateinit var bDayDao: BirthdayDao

    @Inject
    lateinit var infoDao: AdditionalInformationDao

    val idlingRes = CountingIdlingResource("Dao resource")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdayEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        birthday = intent.parcelable(BIRTHDAY)
        mode = Mode.ALL[intent.getIntExtra(
            ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL,
            Mode.DEFAULT_ORDINAL
        )]

        binding.apply {

            additionalInfoRecycler.layoutManager =
                LinearLayoutManager(this@BirthdayEditActivity, RecyclerView.VERTICAL, false)

            birthday?.let { birthday ->
                nameEditText.text?.set(birthday.personName)
                birthdayEdit.year = birthday.year?.value
                birthdayEdit.month = birthday.monthDay.monthValue
                birthdayEdit.day = birthday.monthDay.dayOfMonth
            }

            idlingRes.increment()
            lifecycleScope.launch {
                // Additional information adapter
                val data = birthday?.let { infoDao.getInfoForName(it.personName) } ?: listOf()
                infoAdapter = BirthdayEditInfoAdapter(data)

                // This adapter has 1 item only: the "+" button to add another info
                addInfoAdapter = BirthdayEditAddInfoAdapter()
                addInfoAdapter.actionOnButtonClicked = {
                    infoAdapter.appendInfoItem(
                        AdditionalInformation(0, "Temporary", "")
                    )
                }

                // Use ConcatAdapter to append them
                additionalInfoRecycler.adapter = ConcatAdapter(
                    infoAdapter,
                    addInfoAdapter,
                )

                idlingRes.decrement()
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
            val b = Birthday(name, MonthDay.of(month, day), year?.let { Year.of(it) })
            val info = infoAdapter.data

            // if we're in Edit mode, delete the previous birthday
            when (mode) {
                Mode.ADD -> lifecycleScope.launch {
                    val existing = bDayDao.findByName(name)
                    if (existing != null)
                        showConfirmOverwriteDialog {
                            // a birthday with the same name exists, request confirmation to overwrite
                            lifecycleScope.launch {
                                insertBirthdayAndFinish(b, info)
                            }
                        }
                    else {
                        // fast path: insert & close activity
                        insertBirthdayAndFinish(b, info)
                    }
                }
                Mode.EDIT -> lifecycleScope.launch {
                    if (birthday?.personName != name) {
                        // trying to change the name of the person
                        // if another user already has the same name, show confirmation
                        if (bDayDao.findByName(name) != null)
                            showConfirmOverwriteDialog {
                                lifecycleScope.launch {
                                    renamePersonInBirthdayAndInformation(birthday, b, info)
                                }
                            }
                        else
                            renamePersonInBirthdayAndInformation(birthday, b, info)
                    } else {
                        insertBirthdayAndFinish(b, info)
                    }
                }
            }
        }
    }

    private fun showConfirmOverwriteDialog(onConfirm: () -> Unit) = showConfirmationDialog(
        title = R.string.are_you_sure,
        text = R.string.birthday_will_be_overwritten,
        onConfirm = onConfirm
    )

    private suspend fun renamePersonInBirthdayAndInformation(
        old: Birthday?,
        new: Birthday,
        data: List<AdditionalInformation>,
    ) {
        old?.let { bDayDao.delete(it) }
        //infoDao.delete(*data.toTypedArray()) // deleted by SQL: cascading effect

        insertBirthdayAndFinish(new, data)
    }

    private suspend fun insertBirthdayAndFinish(
        birthday: Birthday,
        info: List<AdditionalInformation>
    ) {
        // update the references to the old name in the AdditionalInformation table
        val newData = info.map {
            if (it.personName != birthday.personName)
                AdditionalInformation(it.id, birthday.personName, it.data)
            else
                it
        }.toTypedArray()

        bDayDao.insertAll(birthday)
        infoDao.insertAll(*newData)

        val data = Intent()
        data.putExtra(BIRTHDAY, birthday)

        setResult(RESULT_OK, data)
        finish()
    }
}