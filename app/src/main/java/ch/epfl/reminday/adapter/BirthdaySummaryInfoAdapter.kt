package ch.epfl.reminday.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.AdditionalInformation
import ch.epfl.reminday.databinding.ActivityBirthdaySummaryInfoItemBinding

class BirthdaySummaryInfoAdapter(
    private val items: List<AdditionalInformation>
) : RecyclerView.Adapter<BirthdaySummaryInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val binding = ActivityBirthdaySummaryInfoItemBinding.bind(view)

        fun bind(info: AdditionalInformation) {
            binding.infoText.text = info.data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_birthday_summary_info_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
