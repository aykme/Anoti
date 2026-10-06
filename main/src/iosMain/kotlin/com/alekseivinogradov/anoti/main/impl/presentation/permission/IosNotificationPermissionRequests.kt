package com.alekseivinogradov.anoti.main.impl.presentation.permission

import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
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
            println("$ANOTI_TAG $TAG: notifications granted: $granted, error: $error")
        }
    }

    override fun openSettings() {
        val settings = NSURL.URLWithString(UIApplicationOpenNotificationSettingsURLString)
        if (settings == null) {
            println("$ANOTI_TAG $TAG: the notification settings address does not parse")
            return
        }
        UIApplication.sharedApplication.openURL(
            url = settings,
            options = emptyMap<Any?, Any?>()
        ) { opened: Boolean ->
            println("$ANOTI_TAG $TAG: notification settings opened: $opened")
        }
    }
}
