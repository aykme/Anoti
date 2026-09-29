package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = HostApplicationFake::class)
class MainActivityRecreationTest {

    private val mainDispatcher = TestMainDispatcher()

    // The activity builds the compose content itself, so the rule only tracks the composition
    // and never launches anything. It shares the clock the stores run on.
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
    fun asksForNotificationPermissionAgainWhenItIsRebuilt() {
        //Given
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //When
        controller.recreate()
        composeRule.waitForIdle()

        //Then
        assertEquals(
            Manifest.permission.POST_NOTIFICATIONS,
            shadowOf(controller.get()).lastRequestedPermission?.requestedPermissions?.single()
        )
    }
}
