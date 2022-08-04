package ch.epfl.reminday.testutils

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

object EspressoMatchers {

    /**
     * Returns a [Matcher] that matches [View]s that have the background of the given color.
     */
    fun withBackgroundColor(@ColorInt color: Int): Matcher<View> = object : BaseMatcher<View>() {
        override fun describeTo(description: Description?) {
            description?.appendText("with background color 0x${color.toUInt().toString(16)}")
        }

        override fun matches(item: Any?): Boolean {
            if (item is View) {
                val bg = item.background as? ColorDrawable
                return bg?.color == color
            }
            return false
        }
    }

    /**
     * Returns a [Matcher] that matches [View]s that have the background corresponding
     * to the given color resource id.
     */
    fun withBackgroundColorRes(@ColorRes res: Int): Matcher<View> {
        val context = getApplicationContext<Context>()
        val color = getColor(context.resources, res, context.theme)
        return withBackgroundColor(color)
    }
}
