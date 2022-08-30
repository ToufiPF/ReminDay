package ch.epfl.reminday.viewmodel.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import ch.epfl.reminday.data.birthday.BirthdayDao
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.MonthDay
import javax.inject.Inject

@HiltViewModel
class BirthdayListViewModel @Inject constructor(
    birthdayDao: BirthdayDao
) : ViewModel() {

    // there's typically ~10 items on screen
    private val pager = Pager(PagingConfig(pageSize = 40, prefetchDistance = 40)) {
        birthdayDao.pagingSourceOrderedByMonthDayYearFrom(MonthDay.from(LocalDate.now()))
    }

    val flow = pager.flow.cachedIn(viewModelScope)
}
