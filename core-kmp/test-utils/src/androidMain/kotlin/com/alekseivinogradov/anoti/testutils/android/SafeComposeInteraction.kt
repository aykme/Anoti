package com.alekseivinogradov.anoti.testutils.android

import androidx.compose.ui.test.SemanticsNodeInteraction
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Retries [interactionCall] until it stops throwing, up to [maxAttempt] times — the Compose
 * Testing equivalent of [safeInteraction], same default retry budget.
 */
@Suppress("TooGenericExceptionThrown")
suspend fun safeComposeInteraction(
    maxAttempt: Int = 60,
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
