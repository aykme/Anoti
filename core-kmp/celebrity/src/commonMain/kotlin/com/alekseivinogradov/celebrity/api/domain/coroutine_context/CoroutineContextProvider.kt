package com.alekseivinogradov.celebrity.api.domain.coroutine_context

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineContextProvider {

    val mainCoroutineContext: CoroutineContext
    val workManagerCoroutineContext: CoroutineContext

    val mainDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
    val ioDispacher: CoroutineContext
    val unconfinedDispatcher: CoroutineDispatcher
}
