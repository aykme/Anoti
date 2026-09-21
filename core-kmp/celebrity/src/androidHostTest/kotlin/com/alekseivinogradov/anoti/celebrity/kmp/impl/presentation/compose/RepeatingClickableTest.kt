package com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@RunWith(RobolectricTestRunner::class)
// Unpinned, Robolectric targets compileSdk and dies inside ApplicationSharedMemory.create.
@Config(sdk = [35])
class RepeatingClickableTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun firesOnceAsSoonAsThePointerGoesDown() {
        //Given
        var clicks = 0
        setContent { clicks++ }

        //When
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { down(center) }
        composeRule.waitForIdle()

        //Then
        assertEquals(1, clicks)
    }

    @Test
    fun repeatsOnlyAfterTheInitialDelayAndThenOncePerRepeatDelay() {
        //Given
        var clicks = 0
        setContent { clicks++ }
        // Otherwise waitForIdle() would pump frames too, and the counts below would depend on how
        // many it happened to need.
        composeRule.mainClock.autoAdvance = false

        //When
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(INITIAL_DELAY_MILLIS)
        val clicksAfterInitialDelay = clicks
        composeRule.mainClock.advanceTimeBy(REPEAT_DELAY_MILLIS)
        val clicksAfterFirstRepeat = clicks
        composeRule.mainClock.advanceTimeBy(REPEAT_DELAY_MILLIS)

        //Then
        assertEquals(1, clicksAfterInitialDelay, "the initial delay must hold back the first repeat")
        assertEquals(2, clicksAfterFirstRepeat)
        assertEquals(clicksAfterFirstRepeat + 1, clicks)
    }

    @Test
    fun stopsRepeatingOnceThePointerGoesUp() {
        //Given
        var clicks = 0
        setContent { clicks++ }

        //When
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(HOLD_MILLIS)
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { up() }
        composeRule.waitForIdle()
        val clicksAtRelease = clicks
        composeRule.mainClock.advanceTimeBy(IDLE_AFTER_RELEASE_MILLIS)
        composeRule.waitForIdle()

        //Then
        assertEquals(clicksAtRelease, clicks)
    }

    @Test
    fun ignoresThePointerEntirelyWhenDisabled() {
        //Given
        var clicks = 0
        setContent(enabled = false) { clicks++ }

        //When
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(HOLD_MILLIS)
        composeRule.waitForIdle()

        //Then
        assertEquals(0, clicks)
    }

    @Test
    fun exposesAButtonClickActionToAccessibilityServices() {
        //Given
        var clicks = 0
        setContent { clicks++ }

        //When
        composeRule.onNodeWithTag(TEST_TAG).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        //Then
        assertEquals(1, clicks)
        composeRule.onNodeWithTag(TEST_TAG).assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        )
    }

    @Test
    fun callsTheLatestOnClickAfterRecomposition() {
        //Given
        var firstCallbackClicks = 0
        var secondCallbackClicks = 0
        var useSecondCallback by mutableStateOf(false)
        composeRule.setContent {
            val interactionSource = remember { MutableInteractionSource() }
            val onClick: () -> Unit = if (useSecondCallback) {
                { secondCallbackClicks++ }
            } else {
                { firstCallbackClicks++ }
            }
            Box(
                Modifier
                    .size(BOX_SIZE_DP.dp)
                    .testTag(TEST_TAG)
                    .repeatingClickable(
                        interactionSource = interactionSource,
                        initialDelayMillis = INITIAL_DELAY_MILLIS,
                        repeatDelayMillis = REPEAT_DELAY_MILLIS,
                        onClick = onClick
                    )
            )
        }

        //When
        useSecondCallback = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        //Then
        assertEquals(0, firstCallbackClicks)
        assertEquals(1, secondCallbackClicks)
    }

    @Test
    fun stopsRepeatingOnceTheGestureIsCancelled() {
        //Given
        var clicks = 0
        setContent { clicks++ }

        //When
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(HOLD_MILLIS)
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { cancel() }
        composeRule.waitForIdle()
        val clicksAtCancel = clicks
        composeRule.mainClock.advanceTimeBy(IDLE_AFTER_RELEASE_MILLIS)
        composeRule.waitForIdle()

        //Then
        assertEquals(clicksAtCancel, clicks)
    }

    @Test
    fun cancelsThePressInteractionWhenDetachedMidGesture() {
        //Given
        val interactions = mutableListOf<Interaction>()
        var enabled by mutableStateOf(true)
        composeRule.setContent {
            val interactionSource = remember { MutableInteractionSource() }
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect { interactions += it }
            }
            Box(
                Modifier
                    .size(BOX_SIZE_DP.dp)
                    .testTag(TEST_TAG)
                    .repeatingClickable(
                        interactionSource = interactionSource,
                        enabled = enabled,
                        initialDelayMillis = INITIAL_DELAY_MILLIS,
                        repeatDelayMillis = REPEAT_DELAY_MILLIS,
                        onClick = {}
                    )
            )
        }

        //When
        composeRule.onNodeWithTag(TEST_TAG).performTouchInput { down(center) }
        composeRule.waitForIdle()
        enabled = false
        composeRule.waitForIdle()

        //Then
        assertEquals(2, interactions.size, "expected one press and one cancel, got $interactions")
        assertIs<PressInteraction.Press>(interactions.first())
        assertIs<PressInteraction.Cancel>(interactions.last())
    }

    @Test
    fun consumesTheDownSoAnAncestorNeverSeesTheHold() {
        //Given
        var ancestorLongClicks = 0
        composeRule.setContent {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { ancestorLongClicks++ }
                )
            ) {
                Box(
                    Modifier
                        .size(BOX_SIZE_DP.dp)
                        .testTag(TEST_TAG)
                        .repeatingClickable(
                            interactionSource = interactionSource,
                            initialDelayMillis = INITIAL_DELAY_MILLIS,
                            repeatDelayMillis = REPEAT_DELAY_MILLIS,
                            onClick = {}
                        )
                )
            }
        }

        //When
        // Unmerged: the ancestor's combinedClickable merges this node's semantics into its own.
        composeRule.onNodeWithTag(TEST_TAG, useUnmergedTree = true)
            .performTouchInput { down(center) }
        composeRule.mainClock.advanceTimeBy(HOLD_MILLIS)
        composeRule.waitForIdle()

        //Then
        assertEquals(0, ancestorLongClicks)
    }

    private fun setContent(enabled: Boolean = true, onClick: () -> Unit) {
        composeRule.setContent {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .size(BOX_SIZE_DP.dp)
                    .testTag(TEST_TAG)
                    .repeatingClickable(
                        interactionSource = interactionSource,
                        enabled = enabled,
                        initialDelayMillis = INITIAL_DELAY_MILLIS,
                        repeatDelayMillis = REPEAT_DELAY_MILLIS,
                        onClick = onClick
                    )
            )
        }
    }
}

private const val TEST_TAG = "repeating_clickable"
private const val BOX_SIZE_DP = 48
private const val INITIAL_DELAY_MILLIS = 400L
private const val REPEAT_DELAY_MILLIS = 160L

// Long enough for several repeats to land while the pointer is held.
private const val HOLD_MILLIS = INITIAL_DELAY_MILLIS + REPEAT_DELAY_MILLIS * 3
private const val IDLE_AFTER_RELEASE_MILLIS = REPEAT_DELAY_MILLIS * 5
