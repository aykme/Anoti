package com.alekseivinogradov.anoti.main.impl.presentation.permission

import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import kotlin.test.Test
import kotlin.test.assertEquals

class IosNotificationPermissionStatusTest {

    @Test
    fun anAppNeverAskedCanAskTheSystem() {
        //Given
        val authorizationStatus = UNAuthorizationStatusNotDetermined

        //When
        val status = notificationPermissionStatusOf(authorizationStatus)

        //Then
        assertEquals(ASKABLE, status)
    }

    @Test
    fun aRefusalLeavesOnlyTheSettings() {
        //Given
        val authorizationStatus = UNAuthorizationStatusDenied

        //When
        val status = notificationPermissionStatusOf(authorizationStatus)

        //Then
        assertEquals(REFUSED, status)
    }

    @Test
    fun everyGrantingStatusCountsAsAllowed() {
        //Given
        val authorizationStatuses = listOf(
            UNAuthorizationStatusAuthorized,
            UNAuthorizationStatusProvisional,
            UNAuthorizationStatusEphemeral
        )

        //When
        val statuses = authorizationStatuses.map(::notificationPermissionStatusOf)

        //Then
        statuses.forEach { status: NotificationPermissionStatus ->
            assertEquals(ALLOWED, status)
        }
    }

    @Test
    fun anUnreadStatusAsksForNothing() {
        //Given
        val authorizationStatus = null

        //When
        val status = notificationPermissionStatusOf(authorizationStatus)

        //Then
        assertEquals(ALLOWED, status)
    }

    private companion object {
        val ASKABLE = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = false
        )

        val REFUSED = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = false,
            isExplanationOwed = false
        )

        val ALLOWED = NotificationPermissionStatus(
            isAllowed = true,
            canPrompt = false,
            isExplanationOwed = false
        )
    }
}
