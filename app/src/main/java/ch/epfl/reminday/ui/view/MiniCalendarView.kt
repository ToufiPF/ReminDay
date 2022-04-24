package ch.epfl.reminday.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat.setImageTintList
import ch.epfl.reminday.R
import ch.epfl.reminday.databinding.ViewMiniCalendarBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.Month
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MiniCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val MONTH_TO_BG_COLOR = hashMapOf(
            Month.JANUARY to R.color.light_blue,
            Month.FEBRUARY to R.color.light_sky_blue,
            Month.MARCH to R.color.light_seagreen,
            Month.APRIL to R.color.light_green,
            Month.MAY to R.color.forest_green,
            Month.JUNE to R.color.orange,
            Month.JULY to R.color.orange_red,
            Month.AUGUST to R.color.medium_violet_red,
            Month.SEPTEMBER to R.color.blue_violet,
            Month.OCTOBER to R.color.indigo,
            Month.NOVEMBER to R.color.rosy_brown,
            Month.DECEMBER to R.color.corn_flower_blue,
        )
    }

    private val binding: ViewMiniCalendarBinding =
        ViewMiniCalendarBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        updateView(null)
    }

    private fun getTintList(@ColorRes color: Int): ColorStateList? =
        ResourcesCompat.getColorStateList(resources, color, context?.theme)

    @Inject
    lateinit var locale: Locale

    private val monthFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("MMM", locale)
    }

    var monthDay: MonthDay? = null
        set(value) {
            field = value
            updateView(value)
        }

    private fun updateView(monthDay: MonthDay?) {
        if (monthDay == null) {
            // ImageViewCompat to set the tint:
            setImageTintList(binding.background, getTintList(R.color.black))

            binding.month.text = null
            binding.day.text = null
        } else {
            val bgColorRes = MONTH_TO_BG_COLOR[monthDay.month]!!
            setImageTintList(binding.background, getTintList(bgColorRes))

            binding.month.text = monthFormatter.format(monthDay)
            binding.day.text = monthDay.dayOfMonth.toString()
        }
    }
}
