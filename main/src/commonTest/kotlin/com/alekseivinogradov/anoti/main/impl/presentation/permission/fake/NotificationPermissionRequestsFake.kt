package com.alekseivinogradov.anoti.main.impl.presentation.permission.fake

import com.alekseivinogradov.anoti.main.impl.presentation.permission.NotificationPermissionRequests

/** Counts the requests a root made instead of showing anything. */
internal class NotificationPermissionRequestsFake : NotificationPermissionRequests {
    var prompts = 0
        private set
    var settingsOpenings = 0
        private set

    override fun prompt() {
        prompts++
    }

    override fun openSettings() {
        settingsOpenings++
    }
}
