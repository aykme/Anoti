package com.alekseivinogradov.anoti.main.impl.presentation.permission

import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationPermissionActionTest {

    @Test
    fun doesNothingWhenNotificationsAreAllowed() {
        //Given
        val status = NotificationPermissionStatus(
            isAllowed = true,
            canPrompt = true,
            isExplanationOwed = false
        )

        //When
        val action = notificationPermissionAction(status)

        //Then
        assertEquals(NotificationPermissionAction.NONE, action)
    }

    @Test
    fun asksTheSystemRightAwayWhenNoExplanationIsOwed() {
        //Given
        val status = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = false
        )

        //When
        val action = notificationPermissionAction(status)

        //Then
        assertEquals(NotificationPermissionAction.PROMPT, action)
    }

    @Test
    fun explainsItselfBeforeAskingWhenTheSystemWantsAnExplanation() {
        //Given
        val status = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = true,
            isExplanationOwed = true
        )

        //When
        val action = notificationPermissionAction(status)

        //Then
        assertEquals(NotificationPermissionAction.EXPLAIN_THEN_PROMPT, action)
    }

    @Test
    fun explainsItselfAndOpensTheSettingsWhenTheSystemCannotAsk() {
        //Given
        val status = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = false,
            isExplanationOwed = false
        )

        //When
        val action = notificationPermissionAction(status)

        //Then
        assertEquals(NotificationPermissionAction.EXPLAIN_THEN_OPEN_SETTINGS, action)
    }

    @Test
    fun allowedNotificationsOutweighEverythingElse() {
        //Given
        val statuses = listOf(
            NotificationPermissionStatus(isAllowed = true, canPrompt = false, isExplanationOwed = true),
            NotificationPermissionStatus(isAllowed = true, canPrompt = true, isExplanationOwed = true),
            NotificationPermissionStatus(isAllowed = true, canPrompt = false, isExplanationOwed = false)
        )

        //When
        val actions = statuses.map(::notificationPermissionAction)

        //Then
        assertEquals(List(statuses.size) { NotificationPermissionAction.NONE }, actions)
    }

    @Test
    fun aSystemThatCannotAskOutweighsAnOwedExplanation() {
        //Given
        val status = NotificationPermissionStatus(
            isAllowed = false,
            canPrompt = false,
            isExplanationOwed = true
        )

        //When
        val action = notificationPermissionAction(status)

        //Then
        assertEquals(NotificationPermissionAction.EXPLAIN_THEN_OPEN_SETTINGS, action)
    }
}
