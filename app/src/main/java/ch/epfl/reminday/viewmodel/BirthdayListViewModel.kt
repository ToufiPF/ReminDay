package ch.epfl.reminday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import ch.epfl.reminday.data.birthday.BirthdayDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BirthdayListViewModel @Inject constructor(
    birthdayDatabase: BirthdayDatabase
) : ViewModel() {

    private val pager = Pager(PagingConfig(pageSize = 20)) {
        birthdayDatabase.birthdayDao().pagingSource()
    }

    val flow = pager.flow.cachedIn(viewModelScope)
}