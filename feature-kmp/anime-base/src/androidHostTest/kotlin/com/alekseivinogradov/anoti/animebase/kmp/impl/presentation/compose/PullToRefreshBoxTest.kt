package com.alekseivinogradov.anoti.animebase.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

private const val BOX_TAG = "pull_to_refresh_box"
private const val CONTENT_TEXT = "Anime list"
private const val SWIPE_MILLIS = 500L
private const val SETTLE_MILLIS = 2000L

@RunWith(RobolectricTestRunner::class)
class PullToRefreshBoxTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var refreshes = 0

    private fun setBox(scrollableContent: Boolean = true) {
        composeRule.setContent {
            AnotiTheme {
                PullToRefreshBox(
                    onRefresh = { refreshes++ },
                    modifier = Modifier.testTag(BOX_TAG).fillMaxSize()
                ) {
                    if (scrollableContent) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { Text(text = CONTENT_TEXT) }
                        }
                    } else {
                        Text(text = CONTENT_TEXT)
                    }
                }
            }
        }
    }

    @Test
    fun theContentItWrapsIsShown() {
        //Given
        setBox()

        //When
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(CONTENT_TEXT).assertIsDisplayed()
    }

    @Test
    fun pullingTheContentDownAsksForARefresh() {
        //Given
        setBox()

        //When
        composeRule.onNodeWithTag(BOX_TAG).performTouchInput {
            swipeDown(startY = top, endY = bottom, durationMillis = SWIPE_MILLIS)
        }
        composeRule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        composeRule.waitForIdle()

        //Then
        assertEquals(1, refreshes)
    }

    @Test
    fun swipingUpAsksForNothing() {
        //Given
        setBox()

        //When
        composeRule.onNodeWithTag(BOX_TAG).performTouchInput { swipeUp() }
        composeRule.waitForIdle()

        //Then
        assertEquals(0, refreshes)
    }

    @Test
    fun aPullTooShortToCrossTheThresholdAsksForNothing() {
        //Given
        setBox()

        //When
        composeRule.onNodeWithTag(BOX_TAG).performTouchInput {
            swipeDown(startY = top, endY = top + 1f)
        }
        composeRule.waitForIdle()

        //Then
        assertEquals(0, refreshes)
    }

    @Test
    fun pullingContentThatDoesNotScrollAsksForNothing() {
        //Given
        setBox(scrollableContent = false)

        //When
        composeRule.onNodeWithTag(BOX_TAG).performTouchInput {
            swipeDown(startY = top, endY = bottom, durationMillis = SWIPE_MILLIS)
        }
        composeRule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        composeRule.waitForIdle()

        //Then
        assertEquals(0, refreshes)
    }
}
