package com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.SemanticsModifierNode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
): Modifier = if (enabled) {
    this then RepeatingClickableElement(
        interactionSource = interactionSource,
        initialDelayMillis = initialDelayMillis,
        repeatDelayMillis = repeatDelayMillis,
        onClick = onClick
    )
} else {
    this
}

private data class RepeatingClickableElement(
    val interactionSource: MutableInteractionSource,
    val initialDelayMillis: Long,
    val repeatDelayMillis: Long,
    val onClick: () -> Unit
) : ModifierNodeElement<RepeatingClickableNode>() {

    override fun create() = RepeatingClickableNode(
        interactionSource = interactionSource,
        initialDelayMillis = initialDelayMillis,
        repeatDelayMillis = repeatDelayMillis,
        onClick = onClick
    )

    override fun update(node: RepeatingClickableNode) {
        node.update(
            interactionSource = interactionSource,
            initialDelayMillis = initialDelayMillis,
            repeatDelayMillis = repeatDelayMillis,
            onClick = onClick
        )
    }
}

private class RepeatingClickableNode(
    private var interactionSource: MutableInteractionSource,
    private var initialDelayMillis: Long,
    private var repeatDelayMillis: Long,
    private var onClick: () -> Unit
) : DelegatingNode(), PointerInputModifierNode, SemanticsModifierNode {

    private var repeatJob: Job? = null

    private val pointerInputNode = delegate(
        SuspendingPointerInputModifierNode {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                // Without consuming, an ancestor's own gesture detector (e.g. the item row's
                // combinedClickable long-click) sees this same press-and-hold as unclaimed and
                // fires alongside it, showing its own press indication too.
                down.consume()
                val press = PressInteraction.Press(down.position)
                // Read once, so the press always ends on the source that started it even if the
                // field is swapped mid-gesture.
                val source = interactionSource
                source.tryEmit(press)
                var up: PointerInputChange? = null
                try {
                    up = waitForUpOrCancellation()
                    up?.consume()
                } finally {
                    // This coroutine can also be canceled outright — by a handler reset or by the
                    // node going away — and then nothing else would close the press.
                    source.tryEmit(
                        if (up != null) {
                            PressInteraction.Release(press)
                        } else {
                            PressInteraction.Cancel(press)
                        }
                    )
                }
            }
        }
    )

    override fun onAttach() {
        startRepeating()
    }

    fun update(
        interactionSource: MutableInteractionSource,
        initialDelayMillis: Long,
        repeatDelayMillis: Long,
        onClick: () -> Unit
    ) {
        this.initialDelayMillis = initialDelayMillis
        this.repeatDelayMillis = repeatDelayMillis
        this.onClick = onClick
        // Only the repeat timer is bound to a source, by collecting from it, so a swap has to
        // restart it. Resetting the gesture too ends any press in flight on the source it began on.
        if (this.interactionSource != interactionSource) {
            this.interactionSource = interactionSource
            pointerInputNode.resetPointerInputHandler()
            startRepeating()
        }
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) = pointerInputNode.onPointerEvent(pointerEvent, pass, bounds)

    override fun onCancelPointerInput() = pointerInputNode.onCancelPointerInput()

    override fun SemanticsPropertyReceiver.applySemantics() {
        role = Role.Button
        // TalkBack's double-tap invokes this action directly, bypassing the gesture above
        // entirely — the repeat-on-hold behavior stays touch-only, but a single activation is
        // reachable.
        onClick {
            this@RepeatingClickableNode.onClick()
            true
        }
    }

    // Repeat timing runs in its own coroutine: the gesture above can't also run a timer while
    // waiting for the pointer to go up.
    private fun startRepeating() {
        repeatJob?.cancel()
        repeatJob = coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction: Interaction ->
                if (interaction is PressInteraction.Press) {
                    onClick()
                    delay(initialDelayMillis.milliseconds)
                    while (true) {
                        delay(repeatDelayMillis.milliseconds)
                        onClick()
                    }
                }
            }
        }
    }
}
