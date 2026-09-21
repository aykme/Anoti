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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class AnotiThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theColorSchemeIsTheAppsOwnPalette() {
        //Given
        var scheme: ColorScheme? = null

        //When
        composeRule.setContent { scheme = anotiColorScheme() }
        composeRule.waitForIdle()

        //Then
        val colorScheme = assertNotNull(scheme)
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
