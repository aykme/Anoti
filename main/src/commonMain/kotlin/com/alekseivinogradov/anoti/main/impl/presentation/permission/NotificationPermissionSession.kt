package com.alekseivinogradov.anoti.main.impl.presentation.permission

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * The notification permission flow of one app process, shared by every root built in it. It lets
 * a root rebuilt over its saved state skip the permission check this process already made, and
 * keep the explanation that was on screen.
 */
class NotificationPermissionSession {

    internal var isChecked: Boolean = false

    /** The explaining action awaiting the user's answer, or `null` while nothing is explained. */
    internal val pendingExplanation: MutableState<NotificationPermissionAction?> =
        mutableStateOf(null)
}
