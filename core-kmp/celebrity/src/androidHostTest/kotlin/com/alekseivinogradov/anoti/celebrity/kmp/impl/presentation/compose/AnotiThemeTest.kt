package com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Black
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.Cinnabar500
import com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose.White
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class AnotiThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun capturedColorScheme(): ColorScheme {
        var scheme: ColorScheme? = null
        composeRule.setContent { scheme = anotiColorScheme() }
        composeRule.waitForIdle()
        return assertNotNull(scheme)
    }

    @Test
    fun theColorSchemeIsTheAppsOwnPalette() {
        //Given / When
        val colorScheme = capturedColorScheme()

        //Then
        assertEquals(Black, colorScheme.background)
        assertEquals(Cinnabar500, colorScheme.primary)
        assertEquals(White, colorScheme.onBackground)
    }

    // The qualifier is the only way to put the emulated device in dark mode, and the whole point
    // of this case is that the app looks the same either way.
    @Config(qualifiers = "night")
    @Test
    fun theColorSchemeIsTheSameWhenTheSystemAsksForDarkMode() {
        //Given / When
        val colorScheme = capturedColorScheme()

        //Then
        assertEquals(Black, colorScheme.background)
        assertEquals(Cinnabar500, colorScheme.primary)
        assertEquals(White, colorScheme.onBackground)
    }

    @Test
    fun contentInsideTheThemeInheritsAColorReadableOnTheBackground() {
        //Given
        var contentColor: Color? = null
        var background: Color? = null

        //When
        composeRule.setContent {
            AnotiTheme {
                contentColor = LocalContentColor.current
                background = MaterialTheme.colorScheme.background
            }
        }
        composeRule.waitForIdle()

        //Then
        // Without the theme's own surface, text falls back to Compose's default black and is
        // invisible against this background.
        assertEquals(White, contentColor)
        assertEquals(Black, background)
    }
}
