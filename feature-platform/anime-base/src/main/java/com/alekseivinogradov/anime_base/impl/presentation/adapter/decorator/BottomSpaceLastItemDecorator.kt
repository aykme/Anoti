package com.alekseivinogradov.anime_base.impl.presentation.adapter.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.celebrity.api.domain.LIST_LAST_ITEM_BOTTOM_PADDING_DP
import com.alekseivinogradov.celebrity.impl.presentation.formatter.px

class BottomSpaceLastItemDecorator : RecyclerView.ItemDecoration() {
    private val bottomPaddingPx = LIST_LAST_ITEM_BOTTOM_PADDING_DP.px().toInt()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0
        if (
            position != RecyclerView.NO_POSITION &&
            itemCount > position &&
            position == itemCount - 1
        ) {
            outRect.bottom = bottomPaddingPx
        }
    }
}
