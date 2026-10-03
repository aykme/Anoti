package com.alekseivinogradov.anoti.testutils.android.api.rule

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Runs a test again when it fails, up to [attempts] tries in all, then fails with the last error.
 * It is for tests that run on a device or reach the live backend, both of which can fail on their
 * own. Every failed try is printed, so a test that passes only on a retry still shows in the log.
 *
 * Put it outside every other rule, so each try starts the screen and the setup anew.
 */
class RetryRule(private val attempts: Int) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            // Any failure counts: an assertion, a timeout, or an error from the screen.
            @Suppress("TooGenericExceptionCaught")
            override fun evaluate() {
                var lastFailure: Throwable? = null
                repeat(attempts) { attempt ->
                    try {
                        base.evaluate()
                        return
                    } catch (failure: Throwable) {
                        lastFailure = failure
                        println(
                            "RetryRule: ${description.displayName} failed on try " +
                                "${attempt + 1} of $attempts: $failure"
                        )
                    }
                }
                throw checkNotNull(lastFailure)
            }
        }
}
