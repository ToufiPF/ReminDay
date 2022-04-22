package ch.epfl.reminday.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.reminday.R
import ch.epfl.reminday.adapter.BirthdayAdapter
import ch.epfl.reminday.ui.view.MarginItemDecoration
import ch.epfl.reminday.viewmodel.fragment.BirthdayListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BirthdayListFragment : Fragment(R.layout.fragment_birthday_list) {

    @Inject
    lateinit var locale: Locale
    private val viewModel: BirthdayListViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BirthdayAdapter(locale)

        recyclerView = view.findViewById(R.id.birthday_list_recycler)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(
            MarginItemDecoration(
                orientation = LinearLayoutManager.VERTICAL,
                verticalSpace = resources.getDimensionPixelSize(R.dimen.small_padding),
                horizontalSpace = 0,
            )
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
}