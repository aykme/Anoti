package com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.impl.presentation.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_negative_button
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_notifications_rationale_message
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_positive_button
import com.alekseivinogradov.anoti.notificationsrationaledialog.kmp.generated.resources.dialog_alert_title
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class NotificationsRationaleDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var dismisses = 0

    private var approvals = 0

    private val title = runBlocking { getString(Res.string.dialog_alert_title) }

    private val message = runBlocking {
        getString(Res.string.dialog_alert_notifications_rationale_message)
    }

    private val negativeButton = runBlocking {
        getString(Res.string.dialog_alert_negative_button)
    }

    private val positiveButton = runBlocking {
        getString(Res.string.dialog_alert_positive_button)
    }

    private fun setDialog() {
        composeRule.setContent {
            AnotiTheme {
                NotificationsRationaleDialog(
                    onDismiss = { dismisses++ },
                    onApprove = { approvals++ }
                )
            }
        }
    }

    @Test
    fun theDialogAsksForNotificationsWithBothAnswersOffered() {
        //Given
        val expectedTexts = listOf(title, message, negativeButton, positiveButton)

        //When
        setDialog()

        //Then
        expectedTexts.forEach { text: String ->
            composeRule.onNodeWithText(text).assertIsDisplayed()
        }
    }

    @Test
    fun decliningReportsADismissalAndNothingElse() {
        //Given
        setDialog()

        //When
        composeRule.onNodeWithText(negativeButton).performClick()

        //Then
        assertEquals(1, dismisses)
        assertEquals(0, approvals)
    }

    @Test
    fun acceptingReportsAnApprovalAndNothingElse() {
        //Given
        setDialog()

        //When
        composeRule.onNodeWithText(positiveButton).performClick()

        //Then
        assertEquals(1, approvals)
        assertEquals(0, dismisses)
    }

    @Test
    fun theDialogStaysUpUntilTheHostTakesItDown() {
        //Given
        setDialog()

        //When
        composeRule.onNodeWithText(negativeButton).performClick()

        //Then
        composeRule.onNodeWithText(title).assertIsDisplayed()
    }
}
