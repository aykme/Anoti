package com.alekseivinogradov.anoti.testutils.android

import androidx.test.espresso.ViewInteraction
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Retries [interactionCall] until it stops throwing, up to [maxAttempt] times.
 *
 * Default budget covers the app's own network-retry envelope (SafeApiImpl: up to 3 attempts,
 * backoff growing to 7.5s) plus real request/render latency, so a slow-but-successful load
 * doesn't flake the assertion.
 */
suspend fun safeInteraction(
    maxAttempt: Int = 60,
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
