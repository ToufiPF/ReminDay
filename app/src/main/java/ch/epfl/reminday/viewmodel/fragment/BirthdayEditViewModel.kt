package ch.epfl.reminday.viewmodel.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.epfl.reminday.format.calendar.MyGregorianCalendar
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import java.time.LocalDate
import java.util.*

class BirthdayEditViewModel : ViewModel() {

    /**
     * Tells whether the edit text for the days should appear before the one for the months.
     */
    fun isDayBeforeMonth(locale: Locale): Boolean {
       return true
    }

    val yearEditContent = MutableLiveData<String?>(null)
    val monthEditContent = MutableLiveData<String?>(null)
    val dayEditContent = MutableLiveData<String?>(null)

    val yearEnabled = MutableLiveData(true)

    private var isYearBeingTyped = false
        set(value) {
            field = value
            updateContents()
        }

    private val calendar = MyGregorianCalendar()

    init {
        val now = LocalDate.now()
        calendar.set(Field.YEAR, now.year)
        calendar.set(Field.MONTH, now.monthValue)
        calendar.set(Field.DAY_OF_MONTH, now.dayOfMonth)
        updateContents()
    }

    fun setField(field: Field, string: String?) {
        if (field == Field.YEAR)
            isYearBeingTyped = !string.isNullOrBlank() && string.length < 4

        val value = string?.toIntOrNull()
        calendar.setNullable(field, value)
        updateContents()
    }

    fun getField(field: Field): Int? = calendar.get(field)

    private fun updateContents() {
        val day = calendar.get(Field.DAY_OF_MONTH)?.toString()
        if (dayEditContent.value != day) dayEditContent.value = day

        val month = calendar.get(Field.MONTH)?.toString()
        if (monthEditContent.value != month) monthEditContent.value = month

        val year = calendar.get(Field.YEAR)?.toString()
        if (!isYearBeingTyped && yearEditContent.value != year)
            yearEditContent.value = year
    }
}