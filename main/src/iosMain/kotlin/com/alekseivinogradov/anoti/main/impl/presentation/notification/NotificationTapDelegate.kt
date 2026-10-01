package com.alekseivinogradov.anoti.main.impl.presentation.notification

import com.alekseivinogradov.anoti.navigation.kmp.NavRootConfig
import platform.Foundation.NSThread
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationDefaultActionIdentifier
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Answers the notification center for the app's notifications: how one shows while the app is
 * open, and where a tap on one leads.
 *
 * @param openFromNotification opens the app on the screen a tapped notification names. Called on
 * the main thread.
 */
internal class NotificationTapDelegate(
    private val openFromNotification: (NavRootConfig) -> Unit
) : NSObject(), UNUserNotificationCenterDelegateProtocol {

    // Into the list with its sound, and no banner over the open app. Android's channel plays the
    // sound and never peeks either.
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionList or UNNotificationPresentationOptionSound
        )
    }

    // Only a tap on the notification itself opens a screen. A payload with no screen in it just
    // brings the app forward.
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val isTap = didReceiveNotificationResponse.actionIdentifier ==
            UNNotificationDefaultActionIdentifier
        val target = notificationTapTarget(
            payload = didReceiveNotificationResponse.notification.request.content.userInfo
        ).takeIf { isTap }
        onMainThread {
            target?.let(openFromNotification)
            withCompletionHandler()
        }
    }
}

// Apple does not say which queue the delegate is called on.
private fun onMainThread(block: () -> Unit) {
    if (NSThread.isMainThread) {
        block()
    } else {
        dispatch_async(dispatch_get_main_queue(), block)
    }
}
