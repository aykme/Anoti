package com.alekseivinogradov.anoti.celebrity.ios.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast.ToastManager
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import platform.Foundation.NSLog

class CoroutineContextProviderPlatform : CoroutineContextProviderBase() {

    private val tag = "Exception Handler"

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        NSLog("$tag: $throwable")
        ToastManager.makeUnknownErrorToast()
    }
}
