package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * Coroutine contexts and dispatchers used across the app, abstracted so platforms/tests can
 * supply their own.
 */
interface CoroutineContextProvider {

    /** Main-thread context with the app's error handler. Carries no `Job` of its own. */
    val mainCoroutineContext: CoroutineContext

    /**
     * [mainCoroutineContext] plus a `Job` shared across the app. Work launched with it survives
     * the cancellation of whatever scope started it.
     */
    val appMainCoroutineContext: CoroutineContext

    /**
     * [mainCoroutineContext] plus a fresh `Job` on every call. The scope it is given to then
     * owns its work and cancels it.
     */
    fun newMainCoroutineContext(): CoroutineContext

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
