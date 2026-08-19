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

/**
 * Fires [onClick] immediately on press, then again every [repeatDelayMillis] after an initial
 * [initialDelayMillis] wait, for as long as the pointer stays down — the touch-and-hold-to-repeat
 * behavior [RepeatListener][com.alekseivinogradov.anoti.celebrity.android.impl.presentation.repeatlistener.RepeatListener]
 * provides for classic Views.
 *
 * The gesture and the repeat timing are split into two independent coroutines: a `pointerInput`
 * gesture only reports press/release into [interactionSource], while a [LaunchedEffect] collects
 * that interaction stream and drives the repeat loop — `AwaitPointerEventScope`, the gesture
 * block's receiver, restricts suspension to itself, so it cannot `launch` a concurrent timer
 * alongside the up-event wait the way a plain coroutine scope could.
 *
 * @param interactionSource receives the press/release [PressInteraction] emitted by the gesture,
 * e.g. for driving a ripple or other visual press feedback.
 * @param enabled when `false`, the gesture and repeat loop are never attached.
 * @param initialDelayMillis how long to wait after the first, immediate [onClick] before
 * repeating starts.
 * @param repeatDelayMillis the delay between each repeated [onClick] once repeating has started.
 * @param onClick invoked once on press, then again on each repeat.
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

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction: Interaction ->
            if (interaction is PressInteraction.Press) {
                currentOnClick()
                delay(initialDelayMillis)
                while (true) {
                    delay(repeatDelayMillis)
                    currentOnClick()
                }
            }
        }
    }

    pointerInput(interactionSource) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val press = PressInteraction.Press(down.position)
            interactionSource.tryEmit(press)
            val up = waitForUpOrCancellation()
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
