package ch.epfl.reminday.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.databinding.ActivityBirthdaySummaryBinding
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.utils.ArgumentNames
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BirthdaySummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBirthdaySummaryBinding

    @Inject
    lateinit var locale: Locale

    private val dateFormatter: DateFormatter by lazy { DateFormatter.longFormatter(locale) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdaySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val birthday: Birthday = intent.getParcelableExtra(ArgumentNames.BIRTHDAY) ?: return
        binding.apply {
            name.text = birthday.personName
            date.text = dateFormatter.format(birthday.monthDay, birthday.year)

            informationRecycler.layoutManager =
                LinearLayoutManager(this@BirthdaySummaryActivity, RecyclerView.VERTICAL, false)
        }
    }
}