package com.alekseivinogradov.anoti.main.impl.presentation

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = HostApplicationFake::class)
class MainActivityOrientationTest {

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
    @Config(qualifiers = PHONE)
    fun keepsAPhoneUpright() {
        //Given
        val intent = plainLaunchingIntent()

        //When
        val controller = composeRule.launchMainActivity(intent)

        //Then
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, controller.get().requestedOrientation)
    }

    @Test
    @Config(qualifiers = TABLET)
    fun letsATabletTurn() {
        //Given
        val intent = plainLaunchingIntent()

        //When
        val controller = composeRule.launchMainActivity(intent)

        //Then
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            controller.get().requestedOrientation
        )
    }

    @Test
    @Config(qualifiers = TABLET_AT_LARGEST_DISPLAY_SIZE)
    fun letsATabletTurnAtTheLargestDisplaySize() {
        //Given
        val intent = plainLaunchingIntent()

        //When
        val controller = composeRule.launchMainActivity(intent)

        //Then
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            controller.get().requestedOrientation
        )
    }

    @Test
    @Config(qualifiers = PHONE)
    fun decidesAgainWhenTheActivityMovesToAWideDisplay() {
        //Given
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())

        //When
        RuntimeEnvironment.setQualifiers(TABLET)
        controller.configurationChange(RuntimeEnvironment.getApplication().resources.configuration)
        composeRule.waitForIdle()

        //Then
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
            controller.get().requestedOrientation
        )
    }

    private companion object {
        const val PHONE = "w411dp-h891dp-mdpi"
        const val TABLET = "w1280dp-h800dp-mdpi"

        // The same tablet drawn twice as large: narrower than 600 dp, wide in its own pixels.
        const val TABLET_AT_LARGEST_DISPLAY_SIZE = "w640dp-h400dp-xhdpi"
    }
}
