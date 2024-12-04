package com.alekseivinogradov.celebrity.api.domain.toast.provider

typealias MakeConnectionErrorToast = () -> Unit
typealias MakeUnknownErrorToast = () -> Unit

class ToastProvider(
    val makeConnectionErrorToast: MakeConnectionErrorToast,
    val makeUnknownErrorToast: MakeUnknownErrorToast
)
