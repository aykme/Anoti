package com.alekseivinogradov.anoti.animebase.android.impl.presentation.adapter.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.LIST_FIRST_INDEX

/**
 * Adds top padding to the first item in a RecyclerView to account for system bars.
 *
 * @param systemBarTopOffset the top offset provided by system window insets.
 */
class EdgeToEdgeItemDecorator(
    private val systemBarTopOffset: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        if (
            parent.getChildAdapterPosition(view) == LIST_FIRST_INDEX &&
            (parent.adapter?.itemCount ?: 0) > LIST_FIRST_INDEX
        ) {
            outRect.top = systemBarTopOffset
        }
    }
}
