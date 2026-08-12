package com.alekseivinogradov.anoti.celebrity.ios.impl.presentation.toast

import platform.Foundation.NSLog

/**
 * No-op placeholder until an iOS UI surface exists — mirrors Android's ToastManager API shape
 * so ToastProvider wiring is identical across platforms.
 */
internal object ToastManager {
    fun makeConnectionErrorToast(): () -> Unit = { NSLog("Toast: connection error") }
    fun makeUnknownErrorToast(): () -> Unit = { NSLog("Toast: unknown error") }
}
