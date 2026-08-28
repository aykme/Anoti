package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val DarkColorScheme = darkColorScheme(
    background = Black,
    surface = Black,
    primary = Cinnabar500,
    surfaceVariant = Grey700,
    onSurfaceVariant = WhiteTransparent,
    onPrimary = White,
    onBackground = White,
    onSurface = White
)

// Anoti has no real light theme yet — LightColorScheme aliases DarkColorScheme so the switch in
// AnotiTheme below is already wired for the day a real light palette replaces this value.
private val LightColorScheme = DarkColorScheme

/**
 * Anoti's Material color scheme, without the full-screen [Surface] [AnotiTheme] wraps it in —
 * for hosting Compose content (e.g. a dialog) that must not be stretched to fill its window.
 */
@Composable
fun anotiColorScheme(): ColorScheme = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme

/**
 * Applies Anoti's Material color scheme and background to [content].
 */
// Composable functions use PascalCase by convention; detekt's FunctionNaming rule expects
// lowerCamelCase.
@Suppress("FunctionNaming")
@Composable
fun AnotiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = anotiColorScheme()) {
        // Surface sets LocalContentColor from the color scheme — without it, plain Text() falls
        // back to Compose's default black, invisible against this theme's black background.
        Surface(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
