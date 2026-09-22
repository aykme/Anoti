package com.alekseivinogradov.anoti.bottomnavigationbar.kmp.impl.presentation.compose

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.BADGE_MAX_NUMBER
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.domain.store.BottomNavigationBarStore
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.BottomNavigationBarUiModel
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.api.presentation.model.SectionUi
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.favorites
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.favorites_new_episodes_description
import com.alekseivinogradov.anoti.bottomnavigationbar.kmp.generated.resources.main
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

private const val MAIN_TAG = "anime_list_button"
private const val FAVORITES_TAG = "anime_favorites_button"
private const val BADGE_NUMBER = 3

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class BottomNavigationBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val dispatchedIntents = mutableListOf<BottomNavigationBarStore.Intent>()

    private fun setBar(
        selectedSection: SectionUi = SectionUi.MAIN,
        favoritesBadgeNumber: Int = 0
    ) {
        composeRule.setContent {
            AnotiTheme {
                BottomNavigationBar(
                    uiModel = BottomNavigationBarUiModel(
                        selectedSection = selectedSection,
                        favoritesBadgeNumber = favoritesBadgeNumber
                    ),
                    dispatch = dispatchedIntents::add
                )
            }
        }
    }

    @Test
    fun bothTabsAreLabeledAndShown() {
        //Given
        val mainLabel = runBlocking { getString(Res.string.main) }
        val favoritesLabel = runBlocking { getString(Res.string.favorites) }

        //When
        setBar()

        //Then
        composeRule.onNodeWithText(mainLabel).assertIsDisplayed()
        composeRule.onNodeWithText(favoritesLabel).assertIsDisplayed()
    }

    @Test
    fun onlyTheSelectedTabReportsItselfSelected() {
        //Given
        val selectedSection = SectionUi.FAVORITES

        //When
        setBar(selectedSection = selectedSection)

        //Then
        composeRule.onNodeWithTag(FAVORITES_TAG).assertIsSelected()
        composeRule.onNodeWithTag(MAIN_TAG).assertIsNotSelected()
    }

    @Test
    fun tappingTheMainTabAsksToGoToMain() {
        //Given
        setBar(selectedSection = SectionUi.FAVORITES)

        //When
        composeRule.onNodeWithTag(MAIN_TAG).performClick()

        //Then
        assertEquals(
            listOf<BottomNavigationBarStore.Intent>(
                BottomNavigationBarStore.Intent.MainSectionClick
            ),
            dispatchedIntents
        )
    }

    @Test
    fun tappingTheFavoritesTabAsksToGoToFavorites() {
        //Given
        setBar()

        //When
        composeRule.onNodeWithTag(FAVORITES_TAG).performClick()

        //Then
        assertEquals(
            listOf<BottomNavigationBarStore.Intent>(
                BottomNavigationBarStore.Intent.FavoritesSectionClick
            ),
            dispatchedIntents
        )
    }

    @Test
    fun tappingTheTabThatIsAlreadySelectedStillAsksToGoThere() {
        //Given
        setBar(selectedSection = SectionUi.MAIN)

        //When
        composeRule.onNodeWithTag(MAIN_TAG).performClick()

        //Then
        assertEquals(
            listOf<BottomNavigationBarStore.Intent>(
                BottomNavigationBarStore.Intent.MainSectionClick
            ),
            dispatchedIntents
        )
    }

    @Test
    fun noBadgeIsShownWhileNothingHasANewEpisode() {
        //Given
        val favoritesBadgeNumber = 0

        //When
        setBar(favoritesBadgeNumber = favoritesBadgeNumber)

        //Then
        composeRule.onNodeWithText("0", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun theBadgeShowsHowManyHaveANewEpisode() {
        //Given
        val favoritesBadgeNumber = BADGE_NUMBER

        //When
        setBar(favoritesBadgeNumber = favoritesBadgeNumber)

        //Then
        composeRule
            .onNodeWithText(BADGE_NUMBER.toString(), useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun theBadgeShowsTheHighestNumberItCanWithoutTheMarker() {
        //Given
        val favoritesBadgeNumber = BADGE_MAX_NUMBER

        //When
        setBar(favoritesBadgeNumber = favoritesBadgeNumber)

        //Then
        composeRule
            .onNodeWithText(BADGE_MAX_NUMBER.toString(), useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun theBadgeCountIsSpokenAloudOnTheFavoritesTab() {
        //Given
        val expected = runBlocking {
            getString(
                Res.string.favorites_new_episodes_description,
                getString(Res.string.favorites),
                BADGE_NUMBER.toString()
            )
        }

        //When
        setBar(favoritesBadgeNumber = BADGE_NUMBER)

        //Then
        composeRule.onNodeWithTag(FAVORITES_TAG).assertContentDescriptionEquals(expected)
    }

    @Test
    fun neitherTabIsDescribedBeyondItsLabelWhileThereIsNoBadge() {
        //Given
        val favoritesBadgeNumber = 0

        //When
        setBar(favoritesBadgeNumber = favoritesBadgeNumber)

        //Then
        composeRule.onNodeWithTag(FAVORITES_TAG).assertContentDescriptionEquals()
        composeRule.onNodeWithTag(MAIN_TAG).assertContentDescriptionEquals()
    }

    @Test
    fun aCappedCountIsSpokenAloudWithItsMarker() {
        //Given
        val expected = runBlocking {
            getString(
                Res.string.favorites_new_episodes_description,
                getString(Res.string.favorites),
                "$BADGE_MAX_NUMBER+"
            )
        }

        //When
        setBar(favoritesBadgeNumber = BADGE_MAX_NUMBER + 1)

        //Then
        composeRule.onNodeWithTag(FAVORITES_TAG).assertContentDescriptionEquals(expected)
    }

    @Test
    fun aCountAboveTheHighestNumberIsCappedAndMarked() {
        //Given
        val favoritesBadgeNumber = BADGE_MAX_NUMBER + 1

        //When
        setBar(favoritesBadgeNumber = favoritesBadgeNumber)

        //Then
        composeRule
            .onNodeWithText("$BADGE_MAX_NUMBER+", useUnmergedTree = true)
            .assertExists()
    }
}
