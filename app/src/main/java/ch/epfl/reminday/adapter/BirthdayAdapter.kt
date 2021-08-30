package ch.epfl.reminday.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.databinding.FragmentBirthdayListItemBinding
import ch.epfl.reminday.format.date.DateFormatter
import ch.epfl.reminday.ui.activity.BirthdaySummaryActivity
import ch.epfl.reminday.utils.ArgumentNames
import java.util.*

class BirthdayAdapter(
    private val locale: Locale
) : PagingDataAdapter<Birthday, BirthdayAdapter.ViewHolder>(DIFF_CALLBACK) {

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
            binding.calendarView.monthDay = birthday.monthDay
            binding.nameView.text = birthday.personName
            binding.dateView.text = dateFormatter.format(birthday.monthDay, birthday.year)

            binding.root.setOnClickListener {
                val intent = Intent(it.context, BirthdaySummaryActivity::class.java)
                intent.putExtra(ArgumentNames.BIRTHDAY, birthday)

                it.context.startActivity(intent)
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