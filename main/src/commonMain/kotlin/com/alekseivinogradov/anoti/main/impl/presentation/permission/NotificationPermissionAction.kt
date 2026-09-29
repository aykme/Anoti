package com.alekseivinogradov.anoti.main.impl.presentation.permission

/** What the screen host does about the notification permission when it starts. */
internal enum class NotificationPermissionAction {
    /** Notifications are allowed already. */
    NONE,

    /** The system's own question is shown right away. */
    PROMPT,

    /** The app explains itself first, and the system's question follows its approval. */
    EXPLAIN_THEN_PROMPT,

    /**
     * The app explains itself first, and its approval opens the app's notification settings,
     * since the system cannot be asked.
     */
    EXPLAIN_THEN_OPEN_SETTINGS
}

/** Picks the [NotificationPermissionAction] for what the platform reported in [status]. */
internal fun notificationPermissionAction(
    status: NotificationPermissionStatus
): NotificationPermissionAction = when {
    status.isAllowed -> NotificationPermissionAction.NONE
    !status.canPrompt -> NotificationPermissionAction.EXPLAIN_THEN_OPEN_SETTINGS
    status.isExplanationOwed -> NotificationPermissionAction.EXPLAIN_THEN_PROMPT
    else -> NotificationPermissionAction.PROMPT
}
