package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = HostApplicationFake::class)
class MainActivityNotificationsRationaleTest {

    private val mainDispatcher = TestMainDispatcher()

    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = mainDispatcher.install()

    @AfterTest
    fun removeTestDispatcher() {
        mainDispatcher.remove()
        assertNoAnimeDetailsRequested()
    }

    @Test
    fun explainsItselfWhenTheSystemSaysAnExplanationIsOwed() {
        //Given
        expectAnExplanation()

        //When
        composeRule.launchMainActivity(plainLaunchingIntent())

        //Then
        composeRule.onNode(isDialog()).assertIsDisplayed()
    }

    @Test
    fun staysSilentUntilTheExplanationIsAccepted() {
        //Given
        expectAnExplanation()

        //When
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //Then
        assertEquals(null, shadowOf(controller.get()).lastRequestedPermission)
    }

    @Test
    fun asksTheSystemOnceTheExplanationIsAccepted() {
        //Given
        expectAnExplanation()
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //When
        composeRule.onNodeWithText(ACCEPT_LABEL).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(
            Manifest.permission.POST_NOTIFICATIONS,
            shadowOf(controller.get()).lastRequestedPermission?.requestedPermissions?.single()
        )
    }

    @Test
    fun dropsTheExplanationWhenItIsRefused() {
        //Given
        expectAnExplanation()
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //When
        composeRule.onNodeWithText(REFUSE_LABEL).performClick()
        composeRule.waitForIdle()

        //Then
        composeRule.onNode(isDialog()).assertDoesNotExist()
        assertEquals(null, shadowOf(controller.get()).lastRequestedPermission)
    }

    private fun expectAnExplanation() {
        shadowOf(RuntimeEnvironment.getApplication().packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS, true)
    }
}

// The dialog is drawn by another module, which ships one set of strings and keeps its resource
// accessors to itself, so its buttons can only be reached here by the words on them.
private const val ACCEPT_LABEL = "Kawaii nya ≽^•⩊•^≼"
private const val REFUSE_LABEL = "Angry nya ฅ^•ﻌ•^ฅ"
