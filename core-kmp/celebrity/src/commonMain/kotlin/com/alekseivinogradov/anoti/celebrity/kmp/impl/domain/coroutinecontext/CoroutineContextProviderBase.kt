package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob

abstract class CoroutineContextProviderBase : CoroutineContextProvider {

    /**
     * Platform implementation of the toast
     */
    abstract val exceptionHandlerCallback: (Throwable) -> Unit

    private val supervisorJob = SupervisorJob()

    /**
     * Default exception handler with toast, log or else platform action
     */
    private val defaultExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _: CoroutineContext, throwable: Throwable ->
            exceptionHandlerCallback(throwable)
        }

    /**
     * Empty exception handler without action
     */
    private val emptyExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _: CoroutineContext, _: Throwable -> }

    override val mainCoroutineContext: CoroutineContext =
        supervisorJob + Dispatchers.Main + defaultExceptionHandler
    override val workManagerCoroutineContext: CoroutineContext =
        supervisorJob + Dispatchers.Default + emptyExceptionHandler

    override val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val ioDispatcher: CoroutineContext = Dispatchers.IO
    override val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
}
