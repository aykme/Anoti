package com.alekseivinogradov.anoti.celebrity.kmp.api.presentation.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

/**
 * Repeats [onClick] while held down. Fires once immediately, then again every
 * [repeatDelayMillis] after [initialDelayMillis].
 *
 * @param interactionSource receives press/release for visual feedback (e.g. a ripple).
 * @param enabled when `false`, disables the gesture entirely.
 * @param initialDelayMillis delay before repeating starts.
 * @param repeatDelayMillis delay between each repeat.
 * @param onClick invoked on press and on each repeat.
 */
fun Modifier.repeatingClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    initialDelayMillis: Long,
    repeatDelayMillis: Long,
    onClick: () -> Unit
): Modifier = composed {
    if (!enabled) {
        return@composed this
    }

    val currentOnClick by rememberUpdatedState(onClick)

    // Repeat timing runs in its own coroutine: the gesture below can't also run a timer while
    // waiting for the pointer to go up.
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction: Interaction ->
            if (interaction is PressInteraction.Press) {
                currentOnClick()
                delay(initialDelayMillis.milliseconds)
                while (true) {
                    delay(repeatDelayMillis.milliseconds)
                    currentOnClick()
                }
            }
        }
    }

    pointerInput(interactionSource) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // Without consuming, an ancestor's own gesture detector (e.g. the item row's
            // combinedClickable long-click) sees this same press-and-hold as unclaimed and
            // fires alongside it, showing its own press indication too.
            down.consume()
            val press = PressInteraction.Press(down.position)
            interactionSource.tryEmit(press)
            val up = waitForUpOrCancellation()
            up?.consume()
            interactionSource.tryEmit(
                if (up != null) {
                    PressInteraction.Release(press)
                } else {
                    PressInteraction.Cancel(press)
                }
            )
        }
    }
}
