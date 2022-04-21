package ch.epfl.reminday.viewmodel.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import ch.epfl.reminday.data.birthday.BirthdayDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BirthdayListViewModel @Inject constructor(
    birthdayDao: BirthdayDao
) : ViewModel() {

    private val pager = Pager(PagingConfig(pageSize = 20)) {
        birthdayDao.pagingSourceOrderedByMonthDayYear()
    }

    val flow = pager.flow.cachedIn(viewModelScope)
}
