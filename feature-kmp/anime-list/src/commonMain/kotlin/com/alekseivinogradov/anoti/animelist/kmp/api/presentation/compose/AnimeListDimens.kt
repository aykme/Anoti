package com.alekseivinogradov.anoti.animelist.kmp.api.presentation.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val POSTER_HEIGHT_DP = 350
const val POSTER_CORNER_PERCENT = 3

// Standard square control size shared by the top bar's filter spacer, search button, search
// field height, and cancel button.
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

// Shifts the search icon so its contour lines up with ic_search_cancel_32, shown in the same
// spot once search opens. The two icons aren't centered identically within their own artwork,
// and OutlinedTextField's trailingIcon slot positions the cancel icon slightly differently than
// a plain Box would.
val SEARCH_ICON_OFFSET_X_DP: Dp = 0.dp

@Suppress("MagicNumber")
val SEARCH_ICON_OFFSET_Y_DP: Dp = (-3).dp

// The number of flexible gaps in an item's score-divider-status-divider-notification chain.
const val GAP_COUNT_WITH_STATUS = 4

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
const val ITEM_PREVIEW_WIDTH_DP = 360
const val ITEM_PREVIEW_HEIGHT_DP = POSTER_HEIGHT_DP + 16
const val TOP_BAR_PREVIEW_WIDTH_DP = 360
const val TOP_BAR_PREVIEW_HEIGHT_DP = 80
