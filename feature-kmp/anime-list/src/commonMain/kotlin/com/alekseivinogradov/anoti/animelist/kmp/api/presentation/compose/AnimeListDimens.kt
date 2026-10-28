package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val POSTER_HEIGHT_DP = 350
const val POSTER_CORNER_PERCENT = 3

// The width from which the anime list lays out in two columns instead of one.
const val TWO_COLUMN_MIN_WIDTH_DP = 600

// The width from which the anime list lays out in three columns, its most.
const val THREE_COLUMN_MIN_WIDTH_DP = 1100

// The side of the top bar's square controls and the search field's minimum height.
const val TOP_BAR_CONTROL_SIZE_DP = 56

// Safety cap on the anime title's line count at large font/display scales; at normal scale the
// title never actually reaches this many lines.
const val TITLE_MAX_LINES = 4

// Line cap for the "Episodes: x / y" line; wraps onto a second line only when it doesn't fit.
const val EPISODES_AVAILABLE_MAX_LINES = 2

// Line cap for the next-episode/beginning-of-show/show-finished line, which forces a line break
// after its label — one line for a wrapped label plus one for the date, with a spare line for
// large font/display scales.
const val EPISODES_EXTRA_MAX_LINES = 3

// Shifts each top-bar icon within its button. It lines the search icon's contour up with
// ic_search_cancel_32, shown in the same spot once search opens. The two aren't centered
// identically within their own artwork, and OutlinedTextField's trailingIcon slot positions the
// cancel icon slightly differently than a plain Box would. One shift keeps every icon of the bar
// at the same height.
val TOP_BAR_ICON_OFFSET_X_DP: Dp = 0.dp

@Suppress("MagicNumber")
val TOP_BAR_ICON_OFFSET_Y_DP: Dp = (-2).dp

// Shifts each top-bar icon's shadow left, so it falls down and to the left like the tab texts'.
val TOP_BAR_ICON_SHADOW_OFFSET_X_DP: Dp = (-1).dp

// The number of flexible gaps in an item's score-divider-status-divider-notification chain.
const val GAP_COUNT_WITH_STATUS = 4

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
const val ITEM_PREVIEW_WIDTH_DP = 360
const val ITEM_PREVIEW_HEIGHT_DP = POSTER_HEIGHT_DP + 16
const val TOP_BAR_PREVIEW_WIDTH_DP = 360
const val TOP_BAR_PREVIEW_HEIGHT_DP = 80
