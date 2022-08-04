package ch.epfl.reminday.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.databinding.FragmentBirthdayListItemBinding
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.ui.activity.BirthdaySummaryActivity
import ch.epfl.reminday.util.constant.ArgumentNames
import java.time.LocalDate
import java.util.*

class BirthdayListAdapter(
    private val locale: Locale
) : PagingDataAdapter<Birthday, BirthdayListAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Birthday>() {
            override fun areItemsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
                return oldItem.personName == newItem.personName
            }

            override fun areContentsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(view: View, locale: Locale) : RecyclerView.ViewHolder(view) {

        private val binding = FragmentBirthdayListItemBinding.bind(view)
        private val dateFormatter = DateFormatter.shortFormatter(locale)

        fun bind(birthday: Birthday) {
            binding.apply {
                calendarView.monthDay = birthday.monthDay
                nameView.text = birthday.personName
                dateView.text = dateFormatter.format(birthday.monthDay, birthday.year)

                root.setOnClickListener {
                    val intent = Intent(it.context, BirthdaySummaryActivity::class.java)
                    intent.putExtra(ArgumentNames.BIRTHDAY, birthday)

                    it.context.startActivity(intent)
                }

                val now = LocalDate.now()
                if (birthday.monthDay.dayOfMonth == now.dayOfMonth &&
                    birthday.monthDay.monthValue == now.monthValue)
                    root.setBackgroundResource(R.color.corn_silk)
                else
                    root.setBackgroundResource(ResourcesCompat.ID_NULL)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_birthday_list_item, parent, false)

        return ViewHolder(view, locale)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val birthday = getItem(position)
        birthday?.let { holder.bind(it) }
    }
}