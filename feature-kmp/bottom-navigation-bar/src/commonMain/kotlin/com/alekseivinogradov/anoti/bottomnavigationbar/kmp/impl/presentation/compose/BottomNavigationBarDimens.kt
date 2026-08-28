package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val NAV_BAR_HEIGHT_DP: Dp = 56.dp
val NAV_BAR_ICON_SIZE_DP: Dp = 24.dp
val NAV_BAR_ELEVATION_DP: Dp = 8.dp

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it. A literal,
// not a reference to NAV_BAR_HEIGHT_DP: annotation arguments must be compile-time constants, and
// Dp isn't one.
const val PREVIEW_WIDTH_DP = 360
const val PREVIEW_HEIGHT_DP = 56
