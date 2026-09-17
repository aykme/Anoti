package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.provider.SystemMessageProvider

/**
 * The app's [CoroutineContextProviderBase]. Its exception handler logs an uncaught throwable and
 * shows the unknown-error system message.
 */
class CoroutineContextProviderDefaultImpl(
    private val systemMessageProvider: SystemMessageProvider
) : CoroutineContextProviderBase() {

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        println("Exception Handler: $throwable")
        systemMessageProvider.makeUnknownErrorSystemMessage()
    }
}
