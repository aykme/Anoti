package com.alekseivinogradov.anoti.main.impl.presentation.compose

import androidx.compose.runtime.State

/**
 * State for the notification-permission rationale dialog overlay, bundled together since
 * [RootContent] only ever passes them along as one unit.
 *
 * @param visible read only inside [NotificationsRationaleOverlay], not [RootContent] itself, so
 * toggling it recomposes just the overlay instead of the whole screen.
 */
internal class NotificationsRationaleState(
    val visible: State<Boolean>,
    val onDismiss: () -> Unit,
    val onApprove: () -> Unit
)
