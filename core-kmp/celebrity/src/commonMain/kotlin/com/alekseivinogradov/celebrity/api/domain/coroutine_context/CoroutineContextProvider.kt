package com.alekseivinogradov.celebrity.api.domain.coroutine_context

import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {

    val mainCoroutineContext: CoroutineContext

    val mainDispatcher: CoroutineDispatcher
    val defaultDispatcher: CoroutineDispatcher
    val ioDispacher: CoroutineContext
    val unconfinedDispatcher: CoroutineDispatcher
}
