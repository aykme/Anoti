package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

const val NEW_EPISODE_SHADOW_RADIUS = 16f
const val NEW_EPISODE_SHADOW_OFFSET = 4f
const val ITEM_MIN_HEIGHT_DP = 146
const val POSTER_WIDTH_DP = 130

// widthDp/heightDp cap a preview's rendering viewport; without both, the unset dimension defaults
// to a full device screen, and AnotiTheme's Surface.fillMaxSize() stretches to fill it.
const val ITEM_PREVIEW_WIDTH_DP = 360
const val ITEM_PREVIEW_HEIGHT_DP = ITEM_MIN_HEIGHT_DP + 16
