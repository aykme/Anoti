package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

const val POSTER_HEIGHT_DP = 350
const val POSTER_CORNER_PERCENT = 3

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
const val ITEM_PREVIEW_WIDTH_DP = 360
const val ITEM_PREVIEW_HEIGHT_DP = POSTER_HEIGHT_DP + 16
const val TOP_BAR_PREVIEW_WIDTH_DP = 360
const val TOP_BAR_PREVIEW_HEIGHT_DP = 80
