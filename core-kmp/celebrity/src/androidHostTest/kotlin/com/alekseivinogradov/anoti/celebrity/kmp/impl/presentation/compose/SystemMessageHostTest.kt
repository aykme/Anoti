package com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.systemmessage.controller.SystemMessageController
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.unknown_error
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class SystemMessageHostTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val controller = SystemMessageController()

    @Test
    fun aMessageSentToTheControllerIsShown() {
        //Given
        val expectedText = runBlocking { getString(Res.string.connection_error) }
        composeRule.setContent { SystemMessageHost(controller) }

        //When
        controller.show(Res.string.connection_error)
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(expectedText).assertIsDisplayed()
    }

    @Test
    fun aNewerMessageReplacesTheOneOnScreen() {
        //Given
        val replacedText = runBlocking { getString(Res.string.connection_error) }
        val expectedText = runBlocking { getString(Res.string.unknown_error) }
        composeRule.setContent { SystemMessageHost(controller) }
        controller.show(Res.string.connection_error)
        composeRule.waitForIdle()

        //When
        controller.show(Res.string.unknown_error)
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(expectedText).assertIsDisplayed()
        composeRule.onNodeWithText(replacedText).assertDoesNotExist()
    }

    @Test
    fun nothingIsShownBeforeAnyMessageArrives() {
        //Given
        val anyMessageText = runBlocking { getString(Res.string.connection_error) }

        //When
        composeRule.setContent { SystemMessageHost(controller) }
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(anyMessageText).assertDoesNotExist()
    }
}
