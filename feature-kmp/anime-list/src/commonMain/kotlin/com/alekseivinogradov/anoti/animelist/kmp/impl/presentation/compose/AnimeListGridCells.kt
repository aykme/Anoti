package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose.TWO_COLUMN_MIN_WIDTH_DP

/**
 * The anime list's columns: one on a list narrower than [TWO_COLUMN_MIN_WIDTH_DP], two from it
 * on. It reads the grid's own width, so a padded grid counts only the room it really has.
 */
internal object AnimeListGridCells : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val count = animeListColumnCount(
            availableWidthPx = availableSize,
            twoColumnMinWidthPx = TWO_COLUMN_MIN_WIDTH_DP.dp.roundToPx()
        )
        return with(GridCells.Fixed(count)) { calculateCrossAxisCellSizes(availableSize, spacing) }
    }
}

/**
 * One column below [twoColumnMinWidthPx], two from it on. Compared in pixels, since a width
 * converted to dp can land a hair under the threshold it exactly meets.
 */
internal fun animeListColumnCount(availableWidthPx: Int, twoColumnMinWidthPx: Int): Int =
    if (availableWidthPx < twoColumnMinWidthPx) 1 else 2
