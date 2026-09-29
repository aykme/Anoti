package com.alekseivinogradov.anoti.main.impl.presentation.permission

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNNotificationSettings
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

private const val TAG = "IosNotificationPermissionStatus"

/**
 * Reads what iOS reports about the app's notifications. The system answers on a background
 * thread, and the caller resumes on its own dispatcher.
 */
internal suspend fun readIosNotificationPermissionStatus(): NotificationPermissionStatus =
    suspendCancellableCoroutine { continuation ->
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings: UNNotificationSettings? ->
                val authorizationStatus = settings?.authorizationStatus
                println("$TAG: authorization status $authorizationStatus")
                continuation.resume(notificationPermissionStatusOf(authorizationStatus))
            }
    }

// iOS asks once, so a refusal leaves only the settings. A status added in a later iOS asks for
// nothing, so the app never shows a dialog it cannot explain.
private fun notificationPermissionStatusOf(
    authorizationStatus: UNAuthorizationStatus?
): NotificationPermissionStatus = when (authorizationStatus) {
    UNAuthorizationStatusNotDetermined -> NotificationPermissionStatus(
        isAllowed = false,
        canPrompt = true,
        isExplanationOwed = false
    )

    UNAuthorizationStatusDenied -> NotificationPermissionStatus(
        isAllowed = false,
        canPrompt = false,
        isExplanationOwed = false
    )

    else -> NotificationPermissionStatus(
        isAllowed = true,
        canPrompt = false,
        isExplanationOwed = false
    )
}
