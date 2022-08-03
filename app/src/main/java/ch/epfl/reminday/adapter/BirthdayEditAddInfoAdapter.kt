package ch.epfl.reminday.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.databinding.ActivityBirthdayEditInfoAddBinding

class BirthdayEditAddInfoAdapter :
    RecyclerView.Adapter<BirthdayEditAddInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    var actionOnButtonClicked: () -> Unit = { }
        set(value) {
            binding?.addInfoButton?.setOnClickListener { value() }
            field = value
        }

    private var binding: ActivityBirthdayEditInfoAddBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ActivityBirthdayEditInfoAddBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding!!.addInfoButton.setOnClickListener { actionOnButtonClicked() }
    }

    override fun getItemCount(): Int = 1
}