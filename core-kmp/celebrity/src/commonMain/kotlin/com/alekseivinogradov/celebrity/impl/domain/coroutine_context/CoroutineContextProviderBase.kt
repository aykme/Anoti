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

    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _: CoroutineContext, trowable: Throwable ->
            exceptionHandlerCallback(trowable)
        }

    override val mainCoroutineContext: CoroutineContext =
        SupervisorJob() + Dispatchers.Main + exceptionHandler

    override val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val ioDispacher: CoroutineContext = Dispatchers.IO
    override val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
}
