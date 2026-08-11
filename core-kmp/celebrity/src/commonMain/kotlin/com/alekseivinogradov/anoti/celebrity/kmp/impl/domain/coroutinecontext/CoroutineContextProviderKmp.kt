package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

class CoroutineContextProviderKmp : CoroutineContextProviderBase() {

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        println("CoroutineContextProviderKmp $throwable")
    }
}
