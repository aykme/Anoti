package com.alekseivinogradov.anoti.main.impl.presentation

import android.Manifest
import android.os.Bundle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Robolectric
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

    @Test
    fun theSearchTextTypedOnTheListComesBackWhenItIsRebuiltFromSavedState() {
        //Given
        val first = composeRule.launchMainActivity(plainLaunchingIntent())
        composeRule.onNodeWithTag(SEARCH_BUTTON_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction()).performTextInput(SEARCH_TEXT)
        composeRule.waitForIdle()
        val saved = Bundle()
        first.saveInstanceState(saved).pause().stop().destroy()

        //When
        Robolectric.buildActivity(MainActivity::class.java, plainLaunchingIntent())
            .create(saved).start().restoreInstanceState(saved).postCreate(saved)
            .resume().visible()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(SEARCH_TEXT).assertIsDisplayed()
    }
}
