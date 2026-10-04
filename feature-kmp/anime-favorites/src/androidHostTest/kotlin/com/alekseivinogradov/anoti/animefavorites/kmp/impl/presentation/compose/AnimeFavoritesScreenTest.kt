package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animefavorites.kmp.api.domain.store.AnimeFavoritesMainStore
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose.ITEM_MIN_HEIGHT_DP
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.compose.POSTER_WIDTH_FRACTION
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.AnimeFavoritesUiModel
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.empty_list_image_description
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes

@RunWith(RobolectricTestRunner::class)
class AnimeFavoritesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

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
        dispatch: (AnimeFavoritesMainStore.Intent) -> Unit = {},
        width: Dp? = null
    ) {
        composeRule.setContent {
            AnotiTheme {
                // A width is required rather than capped: the theme's surface hands its own
                // width down as a minimum.
                Box(width?.let { Modifier.requiredWidth(it) } ?: Modifier) {
                    AnimeFavoritesScreen(
                        uiModel = uiModel,
                        dateFormatter = DateFormatterFake(),
                        dispatch = dispatch
                    )
                }
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
    fun theEmptyStatePictureIsSizedLikeAPosterOnAWideScreen() {
        //Given
        val pictureDescription = runBlocking { getString(Res.string.empty_list_image_description) }
        val rowWidth = UNFOLDED_WIDTH_DP - 2 * EMPTY_STATE_SIDE_PADDING_DP

        //When
        setScreen(
            AnimeFavoritesUiModel(contentType = ContentTypeUi.EMPTY),
            width = UNFOLDED_WIDTH_DP.dp
        )
        composeRule.waitForIdle()

        //Then
        val picture = composeRule.onNodeWithContentDescription(pictureDescription)
            .getUnclippedBoundsInRoot()
        val pictureHeight = picture.bottom - picture.top
        assertEquals(
            rowWidth * POSTER_WIDTH_FRACTION,
            (picture.right - picture.left).value,
            DP_TOLERANCE
        )
        assertTrue(pictureHeight >= ITEM_MIN_HEIGHT_DP.dp, "the picture is $pictureHeight tall")
    }

    @Test
    fun theEmptyStateShowsItsTextOnce() {
        //Given
        val emptyText = runBlocking { getString(Res.string.empty_list) }

        //When
        setScreen(AnimeFavoritesUiModel(contentType = ContentTypeUi.EMPTY))
        composeRule.waitForIdle()

        //Then
        // The merged tree: the panel's measuring copy must not reach it.
        composeRule.onAllNodesWithText(emptyText).assertCountEquals(1)
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

// An unfolded Fold.
private const val UNFOLDED_WIDTH_DP = 852

// The empty state's own start and end padding.
private const val EMPTY_STATE_SIDE_PADDING_DP = 8

// A pixel's rounding at Robolectric's density.
private const val DP_TOLERANCE = 1f
