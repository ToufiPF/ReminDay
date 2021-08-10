package ch.epfl.reminday.viewmodel.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ch.epfl.reminday.format.date.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddBirthdayViewModel @Inject constructor(locale: Locale) : ViewModel() {

    private val patternWithYear: String
    private val patternWithoutYear: String

    init {
        val formatter = DateFormatter.shortFormatter(locale)
        patternWithYear = formatter.pattern(withYear = true)
        patternWithoutYear = formatter.pattern(withYear = false)
    }

    private fun matchesPattern(str: String, pattern: String): Boolean {
        if (str.length > pattern.length) return false

        str.forEachIndexed { idx, c ->
            val p = pattern[idx]
            when {
                // letter in pattern must correspond to digit in input string
                p.isLetter() && !c.isDigit() -> return false
                // separator must be exactly equal to char in input string
                !p.isLetter() && c != p -> return false
            }
        }
        return true
    }

    private val mHintPattern = MutableLiveData(patternWithoutYear)
    val hintPattern: LiveData<String> = mHintPattern

    var yearIncluded: Boolean = false
        set(value) {
            field = value
            mHintPattern.value = if (value) patternWithYear else patternWithoutYear
        }

    fun isInputDateStringValid(str: String): Boolean =
        matchesPattern(str, if (yearIncluded) patternWithYear else patternWithoutYear)

    fun correctInputDateString(str: String): String {
        return ""
    }
}