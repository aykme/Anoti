package com.alekseivinogradov.anoti.main.api.presentation

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.alekseivinogradov.anoti.main.api.di.DiRootDependencies
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.RootLifecycle
import com.alekseivinogradov.anoti.main.impl.presentation.RootSession
import com.alekseivinogradov.anoti.main.impl.presentation.compose.RootSessionContent
import com.alekseivinogradov.anoti.main.impl.presentation.permission.IosNotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.readIosNotificationPermissionStatus
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import platform.UIKit.UIViewController

/**
 * The iOS screen host, the counterpart of `MainActivity`. It shows the app's screen and supplies
 * what only iOS can: the lifecycle and the notification permission. One instance lives for the
 * whole process, and each composition of its screen gets a new root.
 *
 * Main thread only.
 *
 * @param diRootDependencies the app-wide graph every root's graph is built on.
 */
class IosScreenHost(diRootDependencies: DiRootDependencies) {

    private val session = RootSession(
        createDiRootComponent = { createDiRootComponent(parent = diRootDependencies) },
        createLifecycle = ::createRootLifecycle,
        notificationPermissionRequests = IosNotificationPermissionRequests(),
        readNotificationPermissionStatus = ::readIosNotificationPermissionStatus
    )

    /** Builds a new view controller over the app's screen. */
    fun viewController(): UIViewController = ComposeUIViewController(
        configure = {
            // The screen keeps the focused field above the keyboard itself, as on Android.
            // The default would pan the whole view on top of that.
            onFocusBehavior = OnFocusBehavior.DoNothing
        }
    ) {
        RootSessionContent(session = session)
    }
}

// It follows the whole app, since the screen is the app's only one. The root's end destroys it,
// which also drops its observers.
private fun createRootLifecycle(): RootLifecycle {
    val lifecycle = ApplicationLifecycle()
    return RootLifecycle(lifecycle = lifecycle, end = lifecycle::destroy)
}
