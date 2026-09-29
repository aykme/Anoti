package com.alekseivinogradov.anoti.main.impl.presentation.permission

/** The platform's two ways of getting the notification permission granted. */
internal interface NotificationPermissionRequests {

    /** Shows the system's own question about notifications. */
    fun prompt()

    /** Opens the screen where the user switches this app's notifications on. */
    fun openSettings()
}
