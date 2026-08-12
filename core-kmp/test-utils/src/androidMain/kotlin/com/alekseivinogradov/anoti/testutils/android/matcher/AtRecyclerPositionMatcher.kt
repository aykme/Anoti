package com.alekseivinogradov.anoti.testutils.android.matcher

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

/** Matches the item view at [position] inside a [RecyclerView] against [viewMather]. */
class AtRecyclerPositionMatcher(
    private val position: Int,
    private val viewMather: Matcher<View>
) : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

    override fun describeTo(description: Description) {
        description.appendText(
            "Recycler view matcher with position: $position " +
                "and viewMatcher: $viewMather"
        )
        viewMather.describeTo(description)
    }

    override fun matchesSafely(item: RecyclerView): Boolean {
        return viewMather.matches(item.findViewHolderForAdapterPosition(position)?.itemView)
    }
}
