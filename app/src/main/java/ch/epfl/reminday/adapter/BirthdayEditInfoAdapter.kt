package ch.epfl.reminday.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.AdditionalInformation
import ch.epfl.reminday.databinding.ActivityBirthdayEditInfoItemBinding
import ch.epfl.reminday.util.Extensions.set
import java.util.*

class BirthdayEditInfoAdapter(
    originalData: List<AdditionalInformation>
) : RecyclerView.Adapter<BirthdayEditInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ActivityBirthdayEditInfoItemBinding.bind(view)

        fun bind(info: AdditionalInformation, onDeleteClicked: () -> Unit) {
            binding.additionalInfoEditText.apply {
                text?.set(info.data)

                addTextChangedListener {
                    info.data = it?.toString() ?: ""
                }

                setEndDrawableClickListener {
                    Log.i(this::class.simpleName, "Deleting additional info")
                    onDeleteClicked()
                }
            }
        }
    }

    private val mutableData = originalData.toMutableList()

    val data: List<AdditionalInformation>
        get() = Collections.unmodifiableList(mutableData)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_birthday_edit_info_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mutableData[position]) {
            // don't use the fixed position as holder may have been moved around (e.g. after insertion)
            removeInfoItem(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int = mutableData.size

    fun appendInfoItem(info: AdditionalInformation) {
        val idx = mutableData.size
        mutableData.add(idx, info)
        notifyItemInserted(idx)
    }

    fun removeInfoItem(position: Int) {
        mutableData.removeAt(position)
        notifyItemRemoved(position)
    }
}