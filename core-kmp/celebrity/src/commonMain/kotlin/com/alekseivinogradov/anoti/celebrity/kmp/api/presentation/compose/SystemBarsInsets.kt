package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Insets content away from the left and right system bars, leaving both vertical edges untouched.
 */
@Composable
fun Modifier.horizontalSystemBarsPadding(): Modifier =
    windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))

/**
 * The space the system bars take at the top of the window — what content must reserve so it isn't
 * drawn under the status bar.
 */
@Composable
fun systemBarsTopPadding(): Dp = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
