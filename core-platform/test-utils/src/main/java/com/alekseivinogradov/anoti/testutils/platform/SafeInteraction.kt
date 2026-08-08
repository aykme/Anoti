package com.alekseivinogradov.anoti.testutils.platform

import androidx.test.espresso.ViewInteraction
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

suspend fun safeInteraction(
    maxAttempt: Int = 20,
    attemptDelay: Duration = 500.milliseconds,
    interactionCall: () -> ViewInteraction
): ViewInteraction {
    while (true) {
        return try {
            interactionCall()
        } catch (e: Throwable) {
            if (maxAttempt <= 0) {
                throw Throwable(
                    "The number of attempts in Safe Interaction() method " +
                            "has ended with error result: $e"
                )
            } else {
                delay(attemptDelay)
                safeInteraction(
                    maxAttempt = maxAttempt - 1,
                    attemptDelay = attemptDelay,
                    interactionCall = interactionCall
                )
            }
        }
    }
}
