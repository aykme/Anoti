package com.alekseivinogradov.anoti.main.impl.presentation.permission

import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter

private const val TAG = "IosNotificationPermissionRequests"

/** The iOS [NotificationPermissionRequests]: the system's question and the app's settings. */
internal class IosNotificationPermissionRequests : NotificationPermissionRequests {

    // No badge: the app never sets one, and Apple asks for only the options an app uses.
    override fun prompt() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted: Boolean, error: NSError? ->
            println("$TAG: notifications granted: $granted, error: $error")
        }
    }

    override fun openSettings() {
        val settings = NSURL.URLWithString(UIApplicationOpenNotificationSettingsURLString)
            ?: return
        UIApplication.sharedApplication.openURL(
            url = settings,
            options = emptyMap<Any?, Any?>(),
            completionHandler = null
        )
    }
}
