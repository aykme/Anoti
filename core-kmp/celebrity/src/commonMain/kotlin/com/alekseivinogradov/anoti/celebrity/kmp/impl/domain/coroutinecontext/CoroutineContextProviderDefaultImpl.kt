package com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.coroutinecontext

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.controller.ToastController
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error

/**
 * The app's [CoroutineContextProviderBase]. Its exception handler logs an uncaught throwable and
 * shows the unknown-error toast.
 */
class CoroutineContextProviderDefaultImpl(
    private val toastController: ToastController
) : CoroutineContextProviderBase() {

    override val exceptionHandlerCallback: (Throwable) -> Unit = { throwable: Throwable ->
        println("Exception Handler: $throwable")
        toastController.show(Res.string.unknown_error)
    }
}
