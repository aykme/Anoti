package com.alekseivinogradov.anoti.celebrity.kmp.api.domain

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val LIST_FIRST_INDEX = 0
const val FIRST_PAGE = 1
const val ITEMS_PER_PAGE = 20
const val PAGING_PREFETCH_DISTANCE = 10
val SEARCH_DEBOUNCE_MILLISECONDS: Duration = 500L.milliseconds

// Distance to pull down before a refresh triggers, and how far the indicator travels.
val PULL_TO_REFRESH_THRESHOLD: Dp = 114.dp

const val REPEAT_LISTENER_INITIAL_INTERVAL_MILLISECONDS = 500L
const val REPEAT_LISTENER_REPEAT_INTERVAL_MILLISECONDS = 200L
val ANIMATION_DURATION_VERY_SHORT = 250L.milliseconds
val ANIMATION_DURATION_SHORT = 500L.milliseconds
const val LIST_LAST_ITEM_BOTTOM_PADDING_DP = 16F

// Notification toggle FAB size and icon size, shared by every list item card design.
val NOTIFICATION_FAB_SIZE_DP: Dp = 56.dp
val NOTIFICATION_ICON_SIZE_DP: Dp = 40.dp

// Material Subtitle1 text size, shared by every list item card design.
val SUBTITLE1_SP: TextUnit = 16.sp
