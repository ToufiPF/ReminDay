package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.databinding.ActivityBirthdaySummaryBinding
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.ui.activity.utils.BackArrowActivity
import ch.epfl.reminday.utils.ArgumentNames
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BirthdaySummaryActivity : BackArrowActivity() {

    private lateinit var binding: ActivityBirthdaySummaryBinding

    @Inject
    lateinit var locale: Locale

    private val dateFormatter: DateFormatter by lazy { DateFormatter.longFormatter(locale) }

    private lateinit var birthday: Birthday

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdaySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        birthday = intent.getParcelableExtra(ArgumentNames.BIRTHDAY)!!
        binding.apply {
            name.text = birthday.personName
            date.text = dateFormatter.format(birthday.monthDay, birthday.year)

            informationRecycler.layoutManager =
                LinearLayoutManager(this@BirthdaySummaryActivity, RecyclerView.VERTICAL, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_summary_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_birthday_item -> {
                launchEditBirthdayActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchEditBirthdayActivity() {
        val intent = Intent(this, BirthdayEditActivity::class.java)
        intent.putExtra(ArgumentNames.BIRTHDAY, birthday)
        startActivity(intent)
    }
}