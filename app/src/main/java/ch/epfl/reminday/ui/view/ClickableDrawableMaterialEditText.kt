package ch.epfl.reminday.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.BOTTOM
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.LEFT
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.RIGHT
import ch.epfl.reminday.ui.view.ClickableDrawableMaterialEditText.Place.TOP
import com.google.android.material.R
import com.google.android.material.textfield.TextInputEditText

@Suppress("unused")
class ClickableDrawableMaterialEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle,
) : TextInputEditText(context, attrs, defStyleAttr) {

    enum class Place {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM;

        companion object {
            val ALL = listOf(LEFT, TOP, RIGHT, BOTTOM)
        }
    }

    @Suppress("PrivatePropertyName")
    private val START: Int
        get() = if (layoutDirection == LAYOUT_DIRECTION_RTL) RIGHT.ordinal else LEFT.ordinal

    @Suppress("PrivatePropertyName")
    private val END: Int
        get() = if (layoutDirection == LAYOUT_DIRECTION_RTL) LEFT.ordinal else RIGHT.ordinal

    private val listeners: Array<OnClickListener?> = Array(4) { null }

    fun setLeftDrawableClickListener(onClick: OnClickListener) {
        listeners[LEFT.ordinal] = onClick
    }

    fun setTopDrawableClickListener(onClick: OnClickListener) {
        listeners[TOP.ordinal] = onClick
    }

    fun setRightDrawableClickListener(onClick: OnClickListener) {
        listeners[RIGHT.ordinal] = onClick
    }

    fun setBottomDrawableClickListener(onClick: OnClickListener) {
        listeners[BOTTOM.ordinal] = onClick
    }

    fun setStartDrawableClickListener(onClick: OnClickListener) {
        listeners[START] = onClick
    }

    fun setEndDrawableClickListener(onClick: OnClickListener) {
        listeners[END] = onClick
    }

    // performClick called in super.onTouchEvent
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            val halfWidth = width / 2
            val halfHeight = height / 2

            compoundDrawables[LEFT.ordinal]?.let { drawable ->
                listeners[LEFT.ordinal]?.let { listener ->
                    if (paddingLeft <= event.x && event.x <= totalPaddingLeft
                        && halfHeight - drawable.bounds.height() <= event.y
                        && event.y <= halfHeight + drawable.bounds.height()
                    ) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
            compoundDrawables[TOP.ordinal]?.let { drawable ->
                listeners[TOP.ordinal]?.let { listener ->
                    if (paddingTop <= event.y && event.y <= totalPaddingTop
                        && halfWidth - drawable.bounds.width() <= event.x
                        && event.x <= halfWidth + drawable.bounds.width()
                    ) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
            compoundDrawables[RIGHT.ordinal]?.let { drawable ->
                listeners[RIGHT.ordinal]?.let { listener ->
                    if (width - totalPaddingRight <= event.x && event.x <= width - paddingRight
                        && halfHeight - drawable.bounds.height() <= event.y
                        && event.y <= halfHeight + drawable.bounds.height()
                    ) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
            compoundDrawables[BOTTOM.ordinal]?.let { drawable ->
                listeners[BOTTOM.ordinal]?.let { listener ->
                    if (height - totalPaddingBottom <= event.y && event.y <= height - paddingBottom
                        && halfWidth - drawable.bounds.width() <= event.x
                        && event.x <= halfWidth + drawable.bounds.width()
                    ) {
                        listener.onClick(this)
                        return true
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }
}
