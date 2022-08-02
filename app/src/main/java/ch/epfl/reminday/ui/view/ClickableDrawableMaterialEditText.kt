package ch.epfl.reminday.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.ViewCompat
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText

@Suppress("unused")
class ClickableDrawableMaterialEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle,
) : TextInputEditText(context, attrs, defStyleAttr) {

    companion object {
        private const val LEFT = 0
        private const val TOP = 1
        private const val RIGHT = 2
        private const val BOTTOM = 3
    }

    @Suppress("PrivatePropertyName")
    private val START: Int
        get() = if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) RIGHT else LEFT

    @Suppress("PrivatePropertyName")
    private val END: Int
        get() = if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) LEFT else RIGHT

    private val listeners: Array<OnClickListener?> = Array(4) { null }

    fun setLeftDrawableClickListener(onClick: OnClickListener) { listeners[LEFT] = onClick }
    fun setTopDrawableClickListener(onClick: OnClickListener) { listeners[TOP] = onClick }
    fun setRightDrawableClickListener(onClick: OnClickListener) { listeners[RIGHT] = onClick }
    fun setBottomDrawableClickListener(onClick: OnClickListener) { listeners[BOTTOM] = onClick }
    fun setStartDrawableClickListener(onClick: OnClickListener) { listeners[START] = onClick }
    fun setEndDrawableClickListener(onClick: OnClickListener) { listeners[END] = onClick }

    // performClick called in super.onTouchEvent
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val halfWidth = width / 2
            val halfHeight = height / 2

            compoundDrawables[LEFT]?.let { drawable ->
                listeners[LEFT]?.let { listener ->
                    if (paddingLeft <= event.x && event.x <= totalPaddingLeft
                        && halfHeight - drawable.bounds.height() <= event.y
                        && event.y <= halfHeight + drawable.bounds.height()) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
            compoundDrawables[TOP]?.let { drawable ->
                listeners[TOP]?.let { listener ->
                    if (paddingTop <= event.y && event.y <= totalPaddingTop
                        && halfWidth - drawable.bounds.width() <= event.x
                        && event.x <= halfWidth + drawable.bounds.width()) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
            compoundDrawables[RIGHT]?.let { drawable ->
                listeners[RIGHT]?.let { listener ->
                    if (width - totalPaddingRight <= event.x && event.x <= width - paddingRight
                        && halfHeight - drawable.bounds.height() <= event.y
                        && event.y <= halfHeight + drawable.bounds.height()) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
            compoundDrawables[BOTTOM]?.let { drawable ->
                listeners[BOTTOM]?.let { listener ->
                    if (height - totalPaddingBottom <= event.y && event.y <= height - paddingBottom
                        && halfWidth - drawable.bounds.width() <= event.x
                        && event.x <= halfWidth + drawable.bounds.width()) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }
}