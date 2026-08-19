package com.alekseivinogradov.anoti.celebrity.android.impl.presentation.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
private val AnotiBlack = Color(0xFF000000)

@Suppress("MagicNumber")
private val AnotiCoralRed = Color(0xFFE84B3D)

@Suppress("MagicNumber")
private val AnotiDarkGray = Color(0xFF222222)

@Suppress("MagicNumber")
private val AnotiWhiteAlpha = Color(0xD5FFFFFF)

private val DarkColorScheme = darkColorScheme(
    background = AnotiBlack,
    surface = AnotiBlack,
    primary = AnotiCoralRed,
    surfaceVariant = AnotiDarkGray,
    onSurfaceVariant = AnotiWhiteAlpha
)

// Anoti has no real light theme yet — LightColorScheme aliases DarkColorScheme so the switch in
// AnotiTheme below is already wired for the day a real light palette replaces this value.
private val LightColorScheme = DarkColorScheme

@Suppress("FunctionNaming")
@Composable
fun AnotiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
        content = content
    )
}
