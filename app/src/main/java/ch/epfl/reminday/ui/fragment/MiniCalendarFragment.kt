package ch.epfl.reminday.ui.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat.setImageTintList
import androidx.fragment.app.Fragment
import ch.epfl.reminday.R
import ch.epfl.reminday.databinding.FragmentMiniCalendarBinding
import ch.epfl.reminday.format.ordinal.OrdinalFormatter
import java.time.LocalDate
import java.time.Month
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.util.*

class MiniCalendarFragment : Fragment(R.layout.fragment_mini_calendar) {

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

    private fun getColor(@ColorRes color: Int): Int =
        ResourcesCompat.getColor(resources, color, activity?.theme)

    private fun getTintList(@ColorRes color: Int): ColorStateList? =
        ResourcesCompat.getColorStateList(resources, color, activity?.theme)

    // only OK to use between onViewCreated & onViewDestroyed
    private var mBind: FragmentMiniCalendarBinding? = null
    private val bind: FragmentMiniCalendarBinding get() = mBind!!

    private val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
    private val ordinalFormatter = OrdinalFormatter.getInstance(Locale.getDefault())

    var monthDay: MonthDay? = null
        set(value) {
            field = value
            updateView(value)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBind = FragmentMiniCalendarBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBind = null
    }

    private fun updateView(monthDay: MonthDay?) {
        if (monthDay == null) {
            // ImageViewCompat to set the tint:
            setImageTintList(bind.miniCalendarBackground, getTintList(R.color.black))

            bind.miniCalendarMonth.text = null

            bind.miniCalendarDay.text = null
            bind.miniCalendarDayParticle.text = null
        } else {
            val bgColorRes = MONTH_TO_BG_COLOR[monthDay.month]!!
            setImageTintList(bind.miniCalendarBackground, getTintList(bgColorRes))

            bind.miniCalendarMonth.text =
                monthFormatter.format(LocalDate.now().withMonth(monthDay.monthValue))

            bind.miniCalendarDay.text = monthDay.dayOfMonth.toString()
            bind.miniCalendarDayParticle.text = ordinalFormatter.getOrdinalFor(monthDay.dayOfMonth)
        }
    }
}
