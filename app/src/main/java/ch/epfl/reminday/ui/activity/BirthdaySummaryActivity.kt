package ch.epfl.reminday.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.idling.CountingIdlingResource
import ch.epfl.reminday.R
import ch.epfl.reminday.adapter.BirthdaySummaryInfoAdapter
import ch.epfl.reminday.data.birthday.AdditionalInformationDao
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityBirthdaySummaryBinding
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.ui.activity.utils.BackArrowActivity
import ch.epfl.reminday.util.Extensions.showConfirmationDialogWithDoNotAskAgain
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import ch.epfl.reminday.util.constant.PreferenceNames.GeneralPreferenceNames.SKIP_DELETE_CONFIRMATION
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BirthdaySummaryActivity : BackArrowActivity() {

    private lateinit var binding: ActivityBirthdaySummaryBinding

    @Inject
    lateinit var birthdayDao: BirthdayDao

    @Inject
    lateinit var infoDao: AdditionalInformationDao

    @Inject
    lateinit var locale: Locale

    private val dateFormatter: DateFormatter by lazy { DateFormatter.longFormatter(locale) }

    private lateinit var birthday: Birthday

    private val editActivityLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // refresh modified data if EditActivity was exited successfully
            result.data?.getParcelableExtra<Birthday>(BIRTHDAY)?.let {
                intent.putExtra(BIRTHDAY, it)
            }

            recreate()
        }
    }

    val recyclerIdlingResource = CountingIdlingResource("additional_information_idling_res")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdaySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        birthday = intent.getParcelableExtra(BIRTHDAY)!!

        binding.apply {
            name.text = birthday.personName
            date.text = dateFormatter.format(birthday.monthDay, birthday.year)

            additionalInfoRecycler.layoutManager =
                LinearLayoutManager(this@BirthdaySummaryActivity, RecyclerView.VERTICAL, false)
        }

        recyclerIdlingResource.increment()
        lifecycleScope.launch {
            val info = infoDao.getInfoForName(birthday.personName)
            binding.additionalInfoRecycler.adapter = BirthdaySummaryInfoAdapter(info)
            recyclerIdlingResource.decrement()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_summary_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.edit_birthday_item -> {
            launchEditBirthdayActivity()
            true
        }
        R.id.delete_birthday_item -> {
            showConfirmationDialogWithDoNotAskAgain(
                R.string.are_you_sure,
                R.string.delete_birthday_are_you_sure,
                getSharedPreferences(GENERAL_PREFERENCES, Context.MODE_PRIVATE),
                SKIP_DELETE_CONFIRMATION
            ) {
                deleteAndCloseActivity()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun launchEditBirthdayActivity() {
        val intent = Intent(this, BirthdayEditActivity::class.java)
        intent.putExtra(BIRTHDAY, birthday)
        intent.putExtra(BIRTHDAY_EDIT_MODE_ORDINAL, BirthdayEditActivity.Mode.EDIT.ordinal)

        editActivityLauncher.launch(intent)
    }

    private fun deleteAndCloseActivity() {
        lifecycleScope.launch {
            birthdayDao.delete(birthday)
            onBackPressed()
        }
    }
}