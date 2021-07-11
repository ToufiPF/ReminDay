package ch.epfl.reminday.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.data.Birthday
import ch.epfl.reminday.utils.Formatter

class BirthdayAdapter : PagingDataAdapter<Birthday, BirthdayAdapter.ViewHolder>(DIFF_CALLBACK) {

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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val name: TextView = view.findViewById(R.id.birthday_list_item_name_view)
        private val date: TextView = view.findViewById(R.id.birthday_list_item_date_view)

        fun bind(birthday: Birthday) {
            name.text = birthday.personName
            date.text = Formatter.dateToString(birthday.monthDay, birthday.year)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_birthday_list_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val birthday = getItem(position)
        birthday?.let { holder.bind(it) }
    }
}