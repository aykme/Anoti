package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider

/**
 * Holds the callbacks for showing error toasts.
 *
 * @param makeConnectionErrorToast shows that a network/connection error occurred.
 * @param makeUnknownErrorToast shows that an unexpected error occurred.
 */
class ToastProvider(
    val makeConnectionErrorToast: () -> Unit,
    val makeUnknownErrorToast: () -> Unit
)
