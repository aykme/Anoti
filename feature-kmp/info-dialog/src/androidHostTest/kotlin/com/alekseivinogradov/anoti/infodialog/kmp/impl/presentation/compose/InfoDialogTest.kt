package com.alekseivinogradov.anoti.infodialog.kmp.impl.presentation.compose

import android.os.SystemClock
import android.view.MotionEvent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_close
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_privacy_policy
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_support
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.info_dialog_title
import com.alekseivinogradov.anoti.infodialog.kmp.generated.resources.open_in_browser_description
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class InfoDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val calls = mutableListOf<String>()

    private val title = runBlocking { getString(Res.string.info_dialog_title) }

    private val privacyPolicy = runBlocking { getString(Res.string.info_dialog_privacy_policy) }

    private val support = runBlocking { getString(Res.string.info_dialog_support) }

    private val close = runBlocking { getString(Res.string.info_dialog_close) }

    private val openInBrowser = runBlocking { getString(Res.string.open_in_browser_description) }

    private fun setDialog() {
        composeRule.setContent {
            AnotiTheme {
                InfoDialog(
                    onPrivacyPolicyClick = { calls += PRIVACY },
                    onSupportClick = { calls += SUPPORT },
                    onClose = { calls += CLOSE }
                )
            }
        }
        composeRule.waitForIdle()
    }

    // Compose's test input reaches only the dialog's content view. A tap outside reaches the
    // dialog's window, so it goes through the window's decor view, as the system sends it.
    private fun tapOutsideTheDialogWindow() {
        val decorView = checkNotNull(ShadowDialog.getLatestDialog().window).decorView
        val downTime = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action: Int ->
            val event = MotionEvent.obtain(
                /* downTime = */
                downTime,
                /* eventTime = */
                SystemClock.uptimeMillis(),
                /* action = */
                action,
                /* x = */
                OUTSIDE_OFFSET_PX,
                /* y = */
                OUTSIDE_OFFSET_PX,
                /* metaState = */
                0
            )
            decorView.dispatchTouchEvent(event)
            event.recycle()
        }
    }

    @Test
    fun theTitleBothLinksAndCloseAreShown() {
        //Given
        val textsByTag = mapOf(
            TITLE_TAG to title,
            PRIVACY_POLICY_TAG to privacyPolicy,
            SUPPORT_TAG to support,
            CLOSE_TAG to close
        )

        //When
        setDialog()

        //Then
        textsByTag.forEach { (tag: String, text: String) ->
            composeRule.onNodeWithTag(tag).assert(hasText(text)).assertIsDisplayed()
        }
    }

    @Test
    fun eachLinkAndCloseReportOnlyTheirOwnCallback() {
        //Given
        setDialog()

        //When
        composeRule.onNodeWithTag(PRIVACY_POLICY_TAG).performClick()
        composeRule.waitForIdle()
        val callsAfterPrivacyPolicy = calls.toList()
        composeRule.onNodeWithTag(SUPPORT_TAG).performClick()
        composeRule.waitForIdle()
        val callsAfterSupport = calls.toList()
        composeRule.onNodeWithTag(CLOSE_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(listOf(PRIVACY), callsAfterPrivacyPolicy)
        assertEquals(listOf(PRIVACY, SUPPORT), callsAfterSupport)
        assertEquals(listOf(PRIVACY, SUPPORT, CLOSE), calls)
    }

    @Test
    fun backAsksToCloseAndNothingElse() {
        //Given
        setDialog()

        //When
        Espresso.pressBack()
        composeRule.waitForIdle()

        //Then
        assertEquals(listOf(CLOSE), calls)
    }

    @Test
    fun aTapOutsideAsksToCloseAndNothingElse() {
        //Given
        setDialog()

        //When
        tapOutsideTheDialogWindow()
        composeRule.waitForIdle()

        //Then
        assertEquals(listOf(CLOSE), calls)
    }

    @Test
    fun eachLinkOffersToOpenTheBrowserOnATargetOfAtLeast48Dp() {
        //Given
        val opensTheBrowser = SemanticsMatcher("its click is labeled \"$openInBrowser\"") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label == openInBrowser
        }

        //When
        setDialog()

        //Then
        listOf(PRIVACY_POLICY_TAG, SUPPORT_TAG).forEach { tag: String ->
            composeRule.onNodeWithTag(tag)
                .assert(opensTheBrowser)
                .assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    @Config(qualifiers = "w320dp-h240dp-land")
    fun aShortWindowScrollsTheLinksAndKeepsClose() {
        //Given
        RuntimeEnvironment.setFontScale(2f)

        //When
        setDialog()

        //Then
        composeRule.onNode(hasScrollAction() and hasContentToScroll).assertExists()
        composeRule.onNodeWithTag(CLOSE_TAG).assertIsDisplayed()
        listOf(PRIVACY_POLICY_TAG, SUPPORT_TAG).forEach { tag: String ->
            composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun theTitleAndTheLinksStartAtTheSameEdge() {
        //Given
        setDialog()

        //When
        val titleLeft = composeRule.onNodeWithTag(TITLE_TAG).getUnclippedBoundsInRoot().left
        val linkLefts = listOf(PRIVACY_POLICY_TAG, SUPPORT_TAG).map { tag: String ->
            composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().left
        }

        //Then
        assertEquals(listOf(titleLeft, titleLeft), linkLefts)
    }
}

// A scroll action alone exists on any scrollable column; this one also has room to scroll.
private val hasContentToScroll = SemanticsMatcher("has content to scroll") {
    val range = it.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
    range != null && range.maxValue() > 0f
}

private const val TITLE_TAG = "info_dialog_title"
private const val PRIVACY_POLICY_TAG = "info_dialog_privacy_policy"
private const val SUPPORT_TAG = "info_dialog_support"
private const val CLOSE_TAG = "info_dialog_close"

// Above and left of the dialog window, where no part of the dialog is drawn.
private const val OUTSIDE_OFFSET_PX = -10f

private const val PRIVACY = "privacy"
private const val SUPPORT = "support"
private const val CLOSE = "close"
