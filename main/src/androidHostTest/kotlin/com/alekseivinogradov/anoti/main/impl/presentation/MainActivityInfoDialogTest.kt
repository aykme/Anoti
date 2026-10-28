package com.alekseivinogradov.anoti.main.impl.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.PRIVACY_POLICY_URL
import com.alekseivinogradov.anoti.infodialog.kmp.api.domain.SUPPORT_URL
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(application = HostApplicationFake::class)
class MainActivityInfoDialogTest {

    private val mainDispatcher = TestMainDispatcher()

    // The activity builds the compose content itself, so the rule only tracks the composition
    // and never launches anything. It shares the clock the stores run on.
    @get:Rule
    val composeRule = createEmptyComposeRule(StandardTestDispatcher(mainDispatcher.scheduler))

    @BeforeTest
    fun installTestDispatcher() = mainDispatcher.install()

    @AfterTest
    fun removeTestDispatcher() = mainDispatcher.remove()

    private fun tap(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
    }

    /** Every page handed to a browser since the last call, oldest first. */
    private fun startedPages(): List<Intent> =
        generateSequence { shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity }
            .filter { it.action == Intent.ACTION_VIEW }
            .toList()

    @Test
    fun thePrivacyPolicyLinkOpensItsPageAndKeepsTheDialog() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        tap(INFO_BUTTON_TAG)

        //When
        tap(INFO_DIALOG_PRIVACY_POLICY_TAG)

        //Then
        val page = startedPages().single()
        assertEquals(Uri.parse(PRIVACY_POLICY_URL), page.data)
        assertTrue(page.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        composeRule.onNodeWithTag(INFO_DIALOG_TITLE_TAG).assertIsDisplayed()
    }

    @Test
    fun theSupportLinkOpensItsPageAndKeepsTheDialog() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        tap(INFO_BUTTON_TAG)

        //When
        tap(INFO_DIALOG_SUPPORT_TAG)

        //Then
        val page = startedPages().single()
        assertEquals(Uri.parse(SUPPORT_URL), page.data)
        assertTrue(page.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        composeRule.onNodeWithTag(INFO_DIALOG_TITLE_TAG).assertIsDisplayed()
    }

    @Test
    fun closeShutsTheDialogOpensNothingAndLeavesTheInfoButton() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        tap(INFO_BUTTON_TAG)

        //When
        tap(INFO_DIALOG_CLOSE_TAG)

        //Then
        composeRule.onNodeWithTag(INFO_DIALOG_TITLE_TAG).assertDoesNotExist()
        assertEquals(emptyList(), startedPages())
        composeRule.onNodeWithTag(INFO_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun backShutsTheDialogAndKeepsTheActivity() {
        //Given
        val controller = composeRule.launchMainActivity(plainLaunchingIntent())
        tap(INFO_BUTTON_TAG)

        //When
        Espresso.pressBack()
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithTag(INFO_DIALOG_TITLE_TAG).assertDoesNotExist()
        assertFalse(controller.get().isFinishing)
    }

    @Test
    fun theDialogOpensAgainAfterClose() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        tap(INFO_BUTTON_TAG)
        tap(INFO_DIALOG_CLOSE_TAG)

        //When
        tap(INFO_BUTTON_TAG)

        //Then
        composeRule.onNodeWithTag(INFO_DIALOG_TITLE_TAG).assertIsDisplayed()
    }

    @Test
    fun closeAfterALinkShutsTheDialog() {
        //Given
        composeRule.launchMainActivity(plainLaunchingIntent())
        tap(INFO_BUTTON_TAG)
        tap(INFO_DIALOG_PRIVACY_POLICY_TAG)

        //When
        tap(INFO_DIALOG_CLOSE_TAG)

        //Then
        composeRule.onNodeWithTag(INFO_DIALOG_TITLE_TAG).assertDoesNotExist()
    }
}
