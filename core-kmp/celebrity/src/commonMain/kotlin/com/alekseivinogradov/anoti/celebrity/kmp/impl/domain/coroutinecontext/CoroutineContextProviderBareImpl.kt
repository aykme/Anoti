package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG

/**
 * A [CoroutineContextProviderBase] whose exception handler only logs an uncaught throwable. For
 * code that must not show UI, such as tests.
 */
class CoroutineContextProviderBareImpl : CoroutineContextProviderBase() {

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        println("$ANOTI_TAG CoroutineContext: uncaught $throwable")
    }
}
