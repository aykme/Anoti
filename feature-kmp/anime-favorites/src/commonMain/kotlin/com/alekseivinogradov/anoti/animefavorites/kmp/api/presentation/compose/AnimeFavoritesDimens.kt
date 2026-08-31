package com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose

const val NEW_EPISODE_SHADOW_RADIUS = 16f
const val NEW_EPISODE_SHADOW_OFFSET = 4f
const val ITEM_MIN_HEIGHT_DP = 146

// Fraction of the item's own width (poster + info panel, outer margins excluded) the poster
// takes up. A fraction rather than a fixed dp width so it stays proportional as display density
// scales up, instead of eating an ever-larger share of the row and starving the info panel next
// to it.
const val POSTER_WIDTH_FRACTION = 0.38f

// Height of the poster's score bar (score icon + score text + info-type toggle button) when
// everything fits on a single line.
const val SCORE_BAR_HEIGHT_DP = 50

// Safety cap on the anime title's line count at large font/display scales; at normal scale the
// title never actually reaches this many lines.
const val TITLE_MAX_LINES = 3

// Line cap for the "Episodes: x / y" line; wraps onto a second line only when it doesn't fit.
const val EPISODES_MAX_LINES = 2

// Line cap for the next-episode/beginning-of-show/show-finished line, which forces a line break
// after its label — one line for a wrapped label plus one for the date, with a spare line for
// large font/display scales.
const val EXTRA_INFO_MAX_LINES = 3

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
const val ITEM_PREVIEW_WIDTH_DP = 360
const val ITEM_PREVIEW_HEIGHT_DP = ITEM_MIN_HEIGHT_DP + 16
