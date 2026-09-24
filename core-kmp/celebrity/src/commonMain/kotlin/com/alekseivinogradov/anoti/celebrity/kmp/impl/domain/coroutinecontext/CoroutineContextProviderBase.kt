package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.coroutinecontext.CoroutineContextProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class CoroutineContextProviderBase : CoroutineContextProvider {

    /**
     * Receives every throwable that [mainCoroutineContext] and the contexts built from it don't
     * handle.
     */
    abstract val exceptionHandlerCallback: (Throwable) -> Unit

    private val supervisorJob = SupervisorJob()

    /**
     * Exception handler that hands the throwable to [exceptionHandlerCallback].
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

    override val mainCoroutineContext: CoroutineContext = Dispatchers.Main + defaultExceptionHandler

    override val appMainCoroutineContext: CoroutineContext =
        supervisorJob + mainCoroutineContext

    override fun newMainCoroutineContext(): CoroutineContext =
        SupervisorJob() + mainCoroutineContext

    // No Job here: a Job in the context would make this the parent of whatever runs in it, and
    // the worker's own cancellation would stop reaching the work it started.
    override val workManagerCoroutineContext: CoroutineContext =
        Dispatchers.IO + emptyExceptionHandler

    override val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val ioDispatcher: CoroutineContext = Dispatchers.IO
    override val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
}
