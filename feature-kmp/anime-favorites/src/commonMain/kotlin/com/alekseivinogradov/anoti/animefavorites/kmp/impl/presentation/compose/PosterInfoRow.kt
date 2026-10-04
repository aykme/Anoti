package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose.SPACING_UNIT_DP
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose.ITEM_MIN_HEIGHT_DP
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose.POSTER_WIDTH_FRACTION
import kotlin.math.roundToInt

/**
 * A poster beside an info panel, sized the way every favorites row is. The poster takes
 * [POSTER_WIDTH_FRACTION] of the width. Both take the info's natural height, never less than
 * [ITEM_MIN_HEIGHT_DP].
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
internal fun PosterInfoRow(
    poster: @Composable () -> Unit,
    info: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    // The InfoMeasure slot composes the info a second time, solely to learn its natural height.
    // Never being placed keeps that copy out of the tree a screen reader walks. It would still
    // reach a merged parent node and carry every line twice, which clearAndSetSemantics stops.
    // A test reading the unmerged tree still sees the copy and has to filter on isPlaced.
    val infoMeasure: @Composable () -> Unit = {
        Box(Modifier.clearAndSetSemantics {}) {
            info()
        }
    }

    // Row(Modifier.height(IntrinsicSize.Min)) can't be used here. SubcomposeAsyncImage throws on
    // intrinsic measurement, and a plain Image would size the row from its own picture.
    SubcomposeLayout(modifier) { constraints ->
        val minHeightPx = ITEM_MIN_HEIGHT_DP.dp.roundToPx()
        val posterWidthPx = (constraints.maxWidth * POSTER_WIDTH_FRACTION).roundToInt()
        val spacerPx = SPACING_UNIT_DP.roundToPx()
        val infoWidthPx = (constraints.maxWidth - posterWidthPx - spacerPx).coerceAtLeast(0)
        val infoWidthConstraints = Constraints(minWidth = infoWidthPx, maxWidth = infoWidthPx)

        val infoNaturalHeight = subcompose(PosterInfoRowSlot.InfoMeasure, infoMeasure)
            .maxOf { it.measure(infoWidthConstraints).height }
        val rowHeight = maxOf(minHeightPx, infoNaturalHeight)
            .coerceIn(constraints.minHeight, constraints.maxHeight)

        val posterPlaceables = subcompose(PosterInfoRowSlot.Poster, poster)
            .map { it.measure(Constraints.fixed(posterWidthPx, rowHeight)) }
        val infoPlaceables = subcompose(PosterInfoRowSlot.Info, info)
            .map {
                it.measure(
                    infoWidthConstraints.copy(minHeight = rowHeight, maxHeight = rowHeight)
                )
            }

        layout(constraints.maxWidth, rowHeight) {
            posterPlaceables.forEach { it.placeRelative(0, 0) }
            infoPlaceables.forEach { it.placeRelative(posterWidthPx + spacerPx, 0) }
        }
    }
}

private enum class PosterInfoRowSlot { Poster, Info, InfoMeasure }
