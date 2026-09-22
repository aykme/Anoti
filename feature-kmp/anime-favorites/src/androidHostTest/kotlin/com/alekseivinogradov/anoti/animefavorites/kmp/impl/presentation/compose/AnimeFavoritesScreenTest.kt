package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.AnimeFavoritesUiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes

@RunWith(RobolectricTestRunner::class)
class AnimeFavoritesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private object PassThroughDateFormatter : DateFormatter {
        override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
    }

    private val listItem = ListItemUi(
        id = 1,
        imageUrl = null,
        score = "8.42",
        infoType = InfoTypeUi.MAIN,
        name = "Frieren",
        availableEpisodesInfo = "5 / 12",
        releaseStatus = ReleaseStatusUi.ONGOING,
        notification = NotificationUi.ENABLED,
        extraEpisodesInfo = null,
        episodesViewed = "5",
        isNewEpisode = false
    )

    private fun setScreen(
        uiModel: AnimeFavoritesUiModel,
        dispatch: (AnimeFavoritesMainStore.Intent) -> Unit = {}
    ) {
        composeRule.setContent {
            AnotiTheme {
                AnimeFavoritesScreen(
                    uiModel = uiModel,
                    dateFormatter = PassThroughDateFormatter,
                    dispatch = dispatch
                )
            }
        }
    }

    @Test
    fun theLoadingStateShowsTheSpinnerAndNoItems() {
        //Given
        val spinnerDescription = runBlocking { getString(BaseRes.string.loading_in_progress) }

        //When
        setScreen(AnimeFavoritesUiModel(contentType = ContentTypeUi.LOADING))
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithContentDescription(spinnerDescription).assertIsDisplayed()
        composeRule.onNodeWithText(listItem.name).assertDoesNotExist()
    }

    @Test
    fun theEmptyStateShowsTheInvitationToSubscribe() {
        //Given
        val emptyText = runBlocking { getString(Res.string.empty_list) }

        //When
        setScreen(AnimeFavoritesUiModel(contentType = ContentTypeUi.EMPTY))
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText(emptyText).assertIsDisplayed()
    }

    @Test
    fun theLoadedStateShowsEveryItemAndReportsThatTheyWereSubmitted() {
        //Given
        val dispatched = mutableListOf<AnimeFavoritesMainStore.Intent>()

        //When
        setScreen(
            uiModel = AnimeFavoritesUiModel(
                contentType = ContentTypeUi.LOADED,
                listItems = persistentListOf(listItem, listItem.copy(id = 2, name = "Bleach"))
            ),
            dispatch = { dispatched += it }
        )
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText("Frieren").assertIsDisplayed()
        composeRule.onNodeWithText("Bleach").assertIsDisplayed()
        assertEquals(
            listOf<AnimeFavoritesMainStore.Intent>(
                AnimeFavoritesMainStore.Intent.ItemsSubmittedToList
            ),
            dispatched
        )
    }
}
