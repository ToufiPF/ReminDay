package ch.epfl.reminday.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
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
    context: Context,
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

        @ColorInt
        private fun getColor(context: Context, @ColorRes colorRes: Int): Int =
            ResourcesCompat.getColor(context.resources, colorRes, context.theme)
    }

    inner class ViewHolder(view: View, locale: Locale) : RecyclerView.ViewHolder(view) {

        private val binding = FragmentBirthdayListItemBinding.bind(view)
        private val dateFormatter = DateFormatter.shortFormatter(locale)

        fun bind(birthday: Birthday) {
            binding.apply {
                calendarView.monthDay = birthday.monthDay
                nameView.text = birthday.personName
                dateView.text = dateFormatter.format(birthday.monthDay, birthday.year)

                cardView.setOnClickListener {
                    val intent = Intent(it.context, BirthdaySummaryActivity::class.java)
                    intent.putExtra(ArgumentNames.BIRTHDAY, birthday)

                    it.context.startActivity(intent)
                }

                val now = LocalDate.now()
                if (birthday.monthDay.dayOfMonth == now.dayOfMonth &&
                    birthday.monthDay.monthValue == now.monthValue
                ) {
                    cardView.setCardBackgroundColor(highlightColor)
                    cardView.cardElevation = 4.0f
                } else {
                    cardView.setCardBackgroundColor(transparentColor)
                    cardView.cardElevation = 0.0f
                }
            }
        }
    }

    private val highlightColor: Int = getColor(context, R.color.corn_silk)
    private val transparentColor: Int = getColor(context, android.R.color.transparent)

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