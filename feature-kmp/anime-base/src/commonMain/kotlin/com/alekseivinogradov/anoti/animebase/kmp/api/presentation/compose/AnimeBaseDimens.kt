package com.alekseivinogradov.anoti.animebase.kmp.api.presentation.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds

// Distance to pull down before a refresh triggers, and how far the indicator travels.
val PULL_TO_REFRESH_THRESHOLD: Dp = 114.dp

const val REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS = 500L
const val REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS = 200L
val ANIMATION_DURATION_VERY_SHORT = 250L.milliseconds
val ANIMATION_DURATION_SHORT = 500L.milliseconds
const val LIST_LAST_ITEM_BOTTOM_PADDING_DP = 16F

// Notification toggle FAB size and icon size.
val NOTIFICATION_FAB_SIZE_DP: Dp = 56.dp
val NOTIFICATION_ICON_SIZE_DP: Dp = 40.dp

// Alpha of the dark scrim layered behind text/icons over a poster image.
const val POSTER_OVERLAY_ALPHA = 0.5f

// Alpha applied to a score icon and notification button.
const val ITEM_ICON_ALPHA = 0.8f

// Resting elevation of a FloatingActionButton-styled control. Matches Material3's own
// FloatingActionButton default elevation, so a manually styled stand-in (e.g. an IconButton with
// an explicit shadow) can match it exactly.
val FAB_ELEVATION_DP: Dp = 6.dp

// A secondary, smaller circular action button (e.g. an inline info toggle), distinct from the
// larger NOTIFICATION_FAB_SIZE_DP.
val SECONDARY_FAB_SIZE_DP: Dp = 35.dp
val SECONDARY_FAB_ICON_SIZE_DP: Dp = 28.dp

// The base spacing unit used across item layouts.
val SPACING_UNIT_DP: Dp = 8.dp

// Corner rounding percent for a poster-style image.
const val IMAGE_CORNER_PERCENT = 8
