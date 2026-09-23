package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.fake

import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext

/**
 * The app's coroutine contexts with the parts a test needs to control handed in.
 *
 * @param exceptionHandlerCallback receives whatever the contexts do not handle. Swallows it by
 * default, which is what a test asserting something else wants.
 * @param ioDispatcher the context I/O work runs on.
 * @param defaultDispatcher the dispatcher CPU-bound work runs on.
 * @param workManagerCoroutineContext the context background work runs in.
 * Pass a test dispatcher to any of them to keep that work on the test's own scheduler.
 */
class CoroutineContextProviderFake(
    override val exceptionHandlerCallback: (Throwable) -> Unit = {},
    override val ioDispatcher: CoroutineContext = Dispatchers.IO,
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val workManagerCoroutineContext: CoroutineContext = Dispatchers.Default
) : CoroutineContextProviderBase()
