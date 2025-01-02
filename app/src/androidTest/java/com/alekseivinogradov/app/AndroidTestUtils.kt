package com.alekseivinogradov.app

import androidx.test.espresso.ViewInteraction
import kotlinx.coroutines.delay

internal suspend fun safeInteraction(
    maxAttempt: Int = 10,
    attemptDelay: Long = 500L,
    isProgressiveDelay: Boolean = true,
    canBeIgnoredIfNotFound: Boolean = false,
    interactionCall: () -> ViewInteraction
): ViewInteraction? {
    var isSuccessfulResult = false
    var callAttempt = 0
    do {
        try {
            val result = interactionCall()
            isSuccessfulResult = true
            return result
        } catch (e: RuntimeException) {
            callAttempt++
            val delay = if (isProgressiveDelay) {
                attemptDelay * callAttempt
            } else {
                attemptDelay
            }
            if (callAttempt < maxAttempt) {
                delay(delay)
            }
        }
    } while (!isSuccessfulResult && callAttempt < maxAttempt)

    if (canBeIgnoredIfNotFound.not()) {
        throw RuntimeException(
            "The number of attempts in Safe Interaction() method " +
                    "has ended without saccess result"
        )
    } else {
        return null
    }
}
