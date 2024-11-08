package com.alekseivinogradov.celebrity.impl.domain.coroutine_context

import com.alekseivinogradov.celebrity.api.domain.coroutine_context.CoroutineContextProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class CoroutineContextProviderBase : CoroutineContextProvider {

    /**
     * Platform implementation of the toast
     */
    abstract val exceptionHandlerCallback: (Throwable) -> Unit

    private val superviserJob = SupervisorJob()

    /**
     * Default exception handler with toast, log or else platform action
     */
    private val defaultExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _: CoroutineContext, trowable: Throwable ->
            exceptionHandlerCallback(trowable)
        }

    /**
     * Empty exception handler without action
     */
    private val emptyExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _: CoroutineContext, _: Throwable -> }

    override val mainCoroutineContext: CoroutineContext =
        superviserJob + Dispatchers.Main + defaultExceptionHandler
    override val workManagerCoroutineContext: CoroutineContext =
        superviserJob + Dispatchers.Default + emptyExceptionHandler

    override val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val ioDispacher: CoroutineContext = Dispatchers.IO
    override val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
}
