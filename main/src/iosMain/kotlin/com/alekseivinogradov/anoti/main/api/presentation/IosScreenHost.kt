package com.alekseivinogradov.anoti.main.api.presentation

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import com.alekseivinogradov.anoti.main.api.di.DiRootDependencies
import com.alekseivinogradov.anoti.main.impl.di.createDiRootComponent
import com.alekseivinogradov.anoti.main.impl.presentation.IosRootHolder
import com.alekseivinogradov.anoti.main.impl.presentation.compose.IosRootContent
import com.alekseivinogradov.anoti.main.impl.presentation.notification.NotificationTapDelegate
import com.alekseivinogradov.anoti.main.impl.presentation.permission.IosNotificationPermissionRequests
import com.alekseivinogradov.anoti.main.impl.presentation.permission.readIosNotificationPermissionStatus
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import platform.Foundation.NSBundle
import platform.UIKit.UIViewController
import platform.UserNotifications.UNUserNotificationCenter

private const val TAG = "IosScreenHost"

/**
 * The iOS screen host, the counterpart of `MainActivity`. It shows the app's screen and supplies
 * what only iOS can: the lifecycle, the notification permission and the screen a tapped
 * notification names. The scene keeps the screen's state as a string this host writes and reads.
 * One instance lives for the whole process, with one root.
 *
 * Main thread only.
 *
 * @param diRootDependencies the app-wide graph the root's graph is built on.
 */
class IosScreenHost(diRootDependencies: DiRootDependencies) {

    private val holder = IosRootHolder(
        createDiRootComponent = { createDiRootComponent(parent = diRootDependencies) },
        appLifecycle = ApplicationLifecycle(),
        appVersion = appVersion(),
        notificationPermissionRequests = IosNotificationPermissionRequests(),
        readNotificationPermissionStatus = ::readIosNotificationPermissionStatus
    )

    // Held here: the notification center keeps only a weak reference to its delegate.
    private val notificationTapDelegate = NotificationTapDelegate(
        openFromNotification = holder::openFromNotification
    )

    /**
     * Starts what the host serves for the whole process. Called once, before the app finishes
     * launching, so a tap that launched the app reaches the delegate.
     */
    fun start() {
        UNUserNotificationCenter.currentNotificationCenter().delegate = notificationTapDelegate
    }

    /**
     * A view controller over the app's screen. The first call builds the root over
     * [restoredState], the string the scene kept; later calls show the same root.
     */
    fun viewController(restoredState: String?): UIViewController {
        // Tells a scene store that was never written from one read too late.
        println("$ANOTI_TAG $TAG: the scene kept ${restoredState?.length ?: 0} characters")
        val root = holder.rootFor(restoredState)
        return ComposeUIViewController(
            configure = {
                // The screen lifts its message strip above the keyboard by the keyboard's insets.
                // The default would pan the whole view on top of that.
                onFocusBehavior = OnFocusBehavior.DoNothing
            }
        ) {
            IosRootContent(
                holder = holder,
                dependencies = root.dependencies,
                notificationsRationale = root.notificationsRationale
            )
        }
    }

    /**
     * The string for the scene to keep, or `null` while there is no root and the kept one stays.
     */
    fun saveState(): String? = holder.saveState()
}

private fun appVersion(): String {
    val bundle = NSBundle.mainBundle
    val name = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
    val build = bundle.objectForInfoDictionaryKey("CFBundleVersion")
    return "$name ($build)"
}
