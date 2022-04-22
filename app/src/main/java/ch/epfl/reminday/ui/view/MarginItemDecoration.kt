package ch.epfl.reminday.ui.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(
    private val orientation: Int,
    private val verticalSpace: Int = 0,
    private val horizontalSpace: Int = 0,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        outRect.apply {
            top = if (orientation == VERTICAL && position == 0) verticalSpace else 0
            bottom = verticalSpace

            left = if (orientation == HORIZONTAL && position == 0) horizontalSpace else 0
            right = horizontalSpace
        }
    }
}