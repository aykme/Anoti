package com.alekseivinogradov.anoti.main.impl.presentation.permission

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * The notification permission flow of one app process, shared by every root built in it. A root
 * rebuilt over its saved state skips the check this process made. It keeps the explanation that
 * was on screen.
 */
class NotificationPermissionSession {

    internal var isChecked: Boolean = false

    /** The explaining action awaiting the user's answer, or `null` while nothing is explained. */
    internal val pendingExplanation: MutableState<NotificationPermissionAction?> =
        mutableStateOf(null)
}
