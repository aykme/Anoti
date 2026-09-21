package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.State

/**
 * State for the notification-permission rationale dialog overlay, bundled together since
 * [RootContent] only ever passes them along as one unit.
 *
 * @param visible read only by the overlay that draws the dialog, never by [RootContent] itself.
 * Toggling it therefore recomposes just the overlay instead of the whole screen.
 */
internal class NotificationsRationaleState(
    val visible: State<Boolean>,
    val onDismiss: () -> Unit,
    val onApprove: () -> Unit
)
