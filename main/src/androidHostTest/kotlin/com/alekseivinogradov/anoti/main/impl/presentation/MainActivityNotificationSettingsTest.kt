package com.alekseivinogradov.anoti.main.impl.presentation

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alekseivinogradov.anoti.testsdk.MIN_SDK
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

/**
 * Below Tiramisu the system has no notification permission to grant, so the app reads the switch
 * itself and sends the user to the settings screen that holds it. That is the whole mechanism on
 * those versions, not a fallback from another one.
 *
 * The SDK pin is what makes the path reachable: the project-wide Robolectric level is above 33,
 * where this code never runs. It names `minSdk`, the oldest version the app supports.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [MIN_SDK], application = HostApplicationFake::class)
class MainActivityNotificationSettingsTest {

    private val mainDispatcher = TestMainDispatcher()

    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = mainDispatcher.install()

    @AfterTest
    fun removeTestDispatcher() = mainDispatcher.remove()

    @Test
    fun saysNothingWhileNotificationsAreAlreadyOn() {
        //Given
        //Notifications start out enabled.

        //When
        composeRule.launchMainActivity(plainLaunchingIntent())

        //Then
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun explainsItselfWhenNotificationsAreOff() {
        //Given
        turnNotificationsOff()

        //When
        composeRule.launchMainActivity(plainLaunchingIntent())

        //Then
        composeRule.onNode(isDialog()).assertIsDisplayed()
    }

    @Test
    fun opensThisAppsNotificationSettingsWhenTheExplanationIsAccepted() {
        //Given
        turnNotificationsOff()
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //When
        composeRule.onNodeWithText(ACCEPT_LABEL).performClick()
        composeRule.waitForIdle()

        //Then
        val started = shadowOf(controller.get()).nextStartedActivity
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, started.action)
        assertEquals(
            RuntimeEnvironment.getApplication().packageName,
            started.getStringExtra(Settings.EXTRA_APP_PACKAGE)
        )
    }

    private fun turnNotificationsOff() {
        val manager = checkNotNull(
            RuntimeEnvironment.getApplication()
                .getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        )
        shadowOf(manager).setNotificationsEnabled(false)
    }
}

// The dialog is drawn by another module, which ships one set of strings and keeps its resource
// accessors to itself, so its button can only be reached here by the words on it.
private const val ACCEPT_LABEL = "Kawaii nya ≽^•⩊•^≼"
