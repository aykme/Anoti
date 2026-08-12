package com.alekseivinogradov.anoti.celebrity.ios.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext.CoroutineContextProviderBase
import platform.Foundation.NSLog

/**
 * iOS has no Toast concept without a UI framework attached (none exists yet for this app).
 * Uncaught-coroutine errors are logged only; revisit once an iOS UI surface exists to show
 * user-facing feedback.
 */
class CoroutineContextProviderPlatform : CoroutineContextProviderBase() {
    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        NSLog("Exception Handler: $throwable")
    }
}
