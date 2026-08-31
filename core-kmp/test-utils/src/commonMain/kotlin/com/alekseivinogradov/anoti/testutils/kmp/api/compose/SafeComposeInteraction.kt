package com.alekseivinogradov.anoti.testutils.kmp.api.compose

import androidx.compose.ui.test.SemanticsNodeInteraction
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Retries [interactionCall] until it stops throwing, up to [maxAttempt] times. Defaults to a 45s
 * total budget — enough headroom over SafeApi's own worst-case retry budget (5 attempts, a
 * linearly growing delay, and a bounded per-attempt timeout) for interactions that wait on a
 * real network-backed screen to finish loading.
 */
@Suppress("TooGenericExceptionThrown")
suspend fun safeComposeInteraction(
    maxAttempt: Int = 90,
    attemptDelay: Duration = 500.milliseconds,
    interactionCall: () -> SemanticsNodeInteraction
): SemanticsNodeInteraction {
    while (true) {
        return try {
            interactionCall()
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Throwable
        ) {
            if (maxAttempt <= 0) {
                throw Throwable(
                    "The number of attempts in safeComposeInteraction() has ended with error " +
                        "result: $e"
                )
            } else {
                delay(attemptDelay)
                safeComposeInteraction(
                    maxAttempt = maxAttempt - 1,
                    attemptDelay = attemptDelay,
                    interactionCall = interactionCall
                )
            }
        }
    }
}
