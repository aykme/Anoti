package com.alekseivinogradov.anoti.animebackgroundupdate.kmp.impl.domain.scheduler

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Reports an outcome once and drops every later attempt. A background task that finishes just
 * as the platform expires it would otherwise be completed twice, which the platform treats as
 * a programming error.
 *
 * The two outcomes arrive on different threads, so the flag is compared and set in one step. A
 * plain read followed by write would let both through.
 *
 * @param report receives the first outcome handed in, and nothing after that.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class OneShotCompletion(private val report: (success: Boolean) -> Unit) {

    private val reported = AtomicBoolean(false)

    /** Reports [success], unless something was reported already. */
    fun complete(success: Boolean) {
        if (reported.compareAndSet(expectedValue = false, newValue = true)) {
            report(success)
        }
    }
}
