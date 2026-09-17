package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider.ToastProvider

/**
 * The app's [CoroutineContextProviderBase]. Its exception handler logs an uncaught throwable and
 * shows the unknown-error toast.
 */
class CoroutineContextProviderDefaultImpl(
    private val toastProvider: ToastProvider
) : CoroutineContextProviderBase() {

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        println("Exception Handler: $throwable")
        toastProvider.makeUnknownErrorToast()
    }
}
