package com.alekseivinogradov.anoti.main.impl.presentation.permission

/**
 * What the platform reports about the app's permission to post notifications.
 *
 * @param isAllowed notifications may be posted now.
 * @param canPrompt the system can be asked to show its own permission question.
 * @param isExplanationOwed the system wants the app to explain itself before it asks.
 */
internal data class NotificationPermissionStatus(
    val isAllowed: Boolean,
    val canPrompt: Boolean,
    val isExplanationOwed: Boolean
)
