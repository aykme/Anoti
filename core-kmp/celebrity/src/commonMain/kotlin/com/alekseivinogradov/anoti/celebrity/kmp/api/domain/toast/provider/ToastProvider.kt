package com.alekseivinogradov.anoti.celebrity.kmp.api.domain.toast.provider

/** Shows a toast telling the user a network/connection error occurred. */
typealias MakeConnectionErrorToast = () -> Unit

/** Shows a toast telling the user an unexpected error occurred. */
typealias MakeUnknownErrorToast = () -> Unit

/**
 * Holds the platform-supplied callbacks for showing error toasts.
 *
 * @param makeConnectionErrorToast shown for connection/network failures.
 * @param makeUnknownErrorToast shown for anything else unexpected.
 */
class ToastProvider(
    val makeConnectionErrorToast: MakeConnectionErrorToast,
    val makeUnknownErrorToast: MakeUnknownErrorToast
)
