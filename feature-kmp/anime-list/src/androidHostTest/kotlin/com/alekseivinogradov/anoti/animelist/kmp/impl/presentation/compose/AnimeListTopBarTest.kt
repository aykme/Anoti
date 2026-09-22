package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.SEARCH_TEXT_MAX_LENGTH
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SearchUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.SectionHatUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.on_air
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_hint
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_off_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_on_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.soon
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AnimeListTopBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val searchState = mutableStateOf(SearchUi.HIDDEN)

    private val clicks = mutableListOf<String>()

    private val reportedSearchTexts = mutableListOf<String>()

    private fun setTopBar(selectedSection: SectionHatUi = SectionHatUi.ONGOINGS) {
        composeRule.setContent {
            AnotiTheme {
                AnimeListTopBar(
                    selectedSection = selectedSection,
                    search = searchState.value,
                    onOngoingClick = { clicks += ONGOING_CLICK },
                    onAnnouncedClick = { clicks += ANNOUNCED_CLICK },
                    onSearchClick = { clicks += SEARCH_CLICK },
                    onCancelSearchClick = { clicks += CANCEL_SEARCH_CLICK },
                    onSearchTextChange = { reportedSearchTexts += it }
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun typeIntoSearchField(text: String) {
        composeRule.onNode(hasSetTextAction()).performTextInput(text)
        composeRule.waitForIdle()
    }

    @Test
    fun theTabsAreShownWhileTheSearchBarIsHidden() {
        //Given
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        val announcedTabText = runBlocking { getString(Res.string.soon) }
        val searchHint = runBlocking { getString(Res.string.search_hint) }
        searchState.value = SearchUi.HIDDEN

        //When
        setTopBar()

        //Then
        composeRule.onNodeWithText(ongoingTabText).assertIsDisplayed()
        composeRule.onNodeWithText(announcedTabText).assertIsDisplayed()
        composeRule.onNodeWithText(searchHint).assertDoesNotExist()
    }

    @Test
    fun theSearchFieldReplacesTheTabsWhileTheSearchBarIsShown() {
        //Given
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        val searchHint = runBlocking { getString(Res.string.search_hint) }
        searchState.value = SearchUi.SHOWN

        //When
        setTopBar(selectedSection = SectionHatUi.SEARCH)

        //Then
        composeRule.onNodeWithText(searchHint).assertIsDisplayed()
        composeRule.onNodeWithText(ongoingTabText).assertDoesNotExist()
    }

    @Test
    fun eachTabReportsItsOwnClick() {
        //Given
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        val announcedTabText = runBlocking { getString(Res.string.soon) }
        val searchDescription = runBlocking { getString(Res.string.search_on_description) }
        searchState.value = SearchUi.HIDDEN
        setTopBar()

        //When
        composeRule.onNodeWithText(ongoingTabText).performClick()
        composeRule.onNodeWithText(announcedTabText).performClick()
        composeRule.onNodeWithContentDescription(searchDescription).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(listOf(ONGOING_CLICK, ANNOUNCED_CLICK, SEARCH_CLICK), clicks)
    }

    @Test
    fun theCancelButtonReportsItsClick() {
        //Given
        val cancelDescription = runBlocking { getString(Res.string.search_off_description) }
        searchState.value = SearchUi.SHOWN
        setTopBar(selectedSection = SectionHatUi.SEARCH)

        //When
        composeRule.onNodeWithContentDescription(cancelDescription).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(listOf(CANCEL_SEARCH_CLICK), clicks)
    }

    @Test
    fun typingReportsTheNewTextAndShowsItInTheField() {
        //Given
        searchState.value = SearchUi.SHOWN
        setTopBar(selectedSection = SectionHatUi.SEARCH)

        //When
        typeIntoSearchField("frieren")

        //Then
        assertEquals("frieren", reportedSearchTexts.last())
        composeRule.onNodeWithText("frieren").assertIsDisplayed()
    }

    @Test
    fun textLongerThanTheMaximumIsRejected() {
        //Given
        val longestAllowedText = "a".repeat(SEARCH_TEXT_MAX_LENGTH)
        searchState.value = SearchUi.SHOWN
        setTopBar(selectedSection = SectionHatUi.SEARCH)

        //When
        typeIntoSearchField(longestAllowedText)
        val reportedAtTheLimit = reportedSearchTexts.last()
        typeIntoSearchField("b")

        //Then
        assertEquals(longestAllowedText, reportedAtTheLimit)
        assertEquals(
            longestAllowedText,
            reportedSearchTexts.last(),
            "one character over the limit must leave the reported text untouched"
        )
        composeRule.onNodeWithText(longestAllowedText).assertIsDisplayed()
    }

    @Test
    fun typedTextSurvivesClosingAndReopeningTheSearchBar() {
        //Given
        searchState.value = SearchUi.SHOWN
        setTopBar(selectedSection = SectionHatUi.SEARCH)
        typeIntoSearchField("frieren")

        //When
        searchState.value = SearchUi.HIDDEN
        composeRule.waitForIdle()
        searchState.value = SearchUi.SHOWN
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText("frieren").assertIsDisplayed()
    }
}

private const val ONGOING_CLICK = "ongoing"
private const val ANNOUNCED_CLICK = "announced"
private const val SEARCH_CLICK = "search"
private const val CANCEL_SEARCH_CLICK = "cancelSearch"
