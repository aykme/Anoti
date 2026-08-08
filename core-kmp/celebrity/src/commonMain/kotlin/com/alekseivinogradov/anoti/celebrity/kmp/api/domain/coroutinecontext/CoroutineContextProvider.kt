package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Coroutine contexts and dispatchers used across the app, abstracted so platforms/tests can
 * supply their own.
 */
interface CoroutineContextProvider {

    /** Context for coroutines driving the UI/presentation layer. */
    val mainCoroutineContext: CoroutineContext

    /** Context for coroutines running inside a WorkManager worker. */
    val workManagerCoroutineContext: CoroutineContext

    /** Dispatcher for main-thread work. */
    val mainDispatcher: CoroutineDispatcher

    /** Dispatcher for CPU-bound background work. */
    val defaultDispatcher: CoroutineDispatcher

    /** Dispatcher for I/O-bound background work. */
    val ioDispatcher: CoroutineContext

    /** Dispatcher not confined to any specific thread. */
    val unconfinedDispatcher: CoroutineDispatcher
}
