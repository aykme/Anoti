package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.diagnostics

import kotlin.time.TimeSource

/**
 * Temporary tracing for the "notification button does not react" report. Every line is prefixed
 * with [TAG] and a millisecond offset from process start, so a logcat dump reads as a timeline.
 * Delete this file and its call sites once the cause is known.
 */
object DiagnosticLog {

    const val TAG = "ANOTI_DIAG"

    private val processStart = TimeSource.Monotonic.markNow()

    fun log(event: String) {
        println("$TAG ${processStart.elapsedNow().inWholeMilliseconds} $event")
    }
}
