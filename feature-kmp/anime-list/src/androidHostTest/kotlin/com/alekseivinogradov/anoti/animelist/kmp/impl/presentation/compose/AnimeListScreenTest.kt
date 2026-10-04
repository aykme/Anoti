package com.alekseivinogradov.anoti.animelist.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.loading_in_progress
import com.alekseivinogradov.anoti.animelist.kmp.api.domain.store.main.AnimeListMainStore
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.AnimeListUiModel
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ContentTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListContentUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.ListItemUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.EpisodesInfoTypeUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animelist.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.on_air
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.search_on_description
import com.alekseivinogradov.anoti.animelist.kmp.generated.resources.soon
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.AnimeId
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.connection_error
import com.alekseivinogradov.anoti.celebrity.kmp.impl.domain.formatter.fake.DateFormatterFake
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes
import com.alekseivinogradov.anoti.celebrity.kmp.generated.resources.Res as CelebrityRes

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class AnimeListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val uiModelState = mutableStateOf(AnimeListUiModel())

    private val dispatched = mutableListOf<AnimeListMainStore.Intent>()

    // Null lets the screen take the emulated window. A size is required rather than capped,
    // since the theme's surface hands its own size down as a minimum.
    private val screenWidthState = mutableStateOf<Dp?>(null)

    private fun listItem(id: AnimeId) = ListItemUi(
        id = id,
        name = "Anime $id",
        imageUrl = null,
        episodesInfoType = EpisodesInfoTypeUi.AVAILABLE,
        episodesAired = 2,
        episodesTotal = 12,
        nextEpisodeAt = null,
        airedOn = null,
        releasedOn = null,
        score = "8.90",
        releaseStatus = ReleaseStatusUi.ONGOING,
        notification = NotificationUi.ENABLED
    )

    private fun loadedModel(itemCount: Int, isNeedToResetListPositon: Boolean = false) =
        AnimeListUiModel(
            contentType = ContentTypeUi.LOADED,
            listContent = ListContentUi(
                listItems = (0 until itemCount).map(::listItem).toPersistentList(),
                isNeedToResetListPositon = isNeedToResetListPositon
            )
        )

    private fun setScreen() {
        composeRule.setContent {
            AnotiTheme {
                val sizeModifier = screenWidthState.value
                    ?.let { Modifier.requiredSize(it, TALL_SCREEN_HEIGHT_DP.dp) }
                    ?: Modifier
                Box(sizeModifier) {
                    AnimeListScreen(
                        uiModel = uiModelState.value,
                        dateFormatter = DateFormatterFake(),
                        dispatch = { dispatched += it }
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun loadNextPageCount() =
        dispatched.count { it == AnimeListMainStore.Intent.LoadNextPage }

    private fun scrollListToIndex(index: Int) {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(index)
        composeRule.waitForIdle()
    }

    private fun titleTop(title: String): Dp =
        composeRule.onNodeWithText(title, useUnmergedTree = true).getUnclippedBoundsInRoot().top

    private fun showListAt(width: Dp, itemCount: Int) {
        screenWidthState.value = width
        uiModelState.value = loadedModel(itemCount = itemCount)
        setScreen()
    }

    @Test
    fun aPhoneWideListStacksItsItems() {
        //Given
        val width = PHONE_WIDTH_DP.dp

        //When
        showListAt(width, itemCount = 4)

        //Then
        assertTrue(
            titleTop("Anime 1") > titleTop("Anime 0"),
            "on a phone the second item must sit below the first"
        )
    }

    @Test
    fun anUnfoldedWideListPutsTwoItemsInARow() {
        //Given
        val width = UNFOLDED_WIDTH_DP.dp

        //When
        showListAt(width, itemCount = 4)

        //Then
        assertEquals(titleTop("Anime 0"), titleTop("Anime 1"), "the first two items share a row")
        assertTrue(titleTop("Anime 2") > titleTop("Anime 0"), "the third item starts a new row")
    }

    @Test
    fun aTabletWideListStillPutsOnlyTwoItemsInARow() {
        //Given
        val width = TABLET_LANDSCAPE_WIDTH_DP.dp

        //When
        showListAt(width, itemCount = 4)

        //Then
        assertEquals(titleTop("Anime 0"), titleTop("Anime 1"), "the first two items share a row")
        assertTrue(titleTop("Anime 2") > titleTop("Anime 0"), "the third item starts a new row")
    }

    @Test
    fun reachingTheEndOfAWideGridAsksForTheNextPage() {
        //Given
        showListAt(TABLET_LANDSCAPE_WIDTH_DP.dp, itemCount = LONG_LIST_ITEM_COUNT)
        val countBeforeScrolling = loadNextPageCount()

        //When
        scrollListToIndex(LONG_LIST_ITEM_COUNT - 1)

        //Then
        assertEquals(0, countBeforeScrolling, "a grid opened at the top has no next page to load")
        assertTrue(loadNextPageCount() >= 1, "reaching the end must ask for the next page")
    }

    @Test
    fun theGridReportsItsRealRowsAndColumns() {
        //Given
        showListAt(UNFOLDED_WIDTH_DP.dp, itemCount = ODD_ITEM_COUNT)

        //When
        val info = composeRule.onNode(hasScrollToIndexAction())
            .fetchSemanticsNode().config[SemanticsProperties.CollectionInfo]

        //Then
        assertEquals(ODD_ITEM_ROW_COUNT, info.rowCount, "the last row holds a single item")
        assertEquals(2, info.columnCount)
    }

    @Test
    fun aWidthChangeKeepsTheFirstVisibleItem() {
        //Given
        showListAt(PHONE_WIDTH_DP.dp, itemCount = LONG_LIST_ITEM_COUNT)
        scrollListToIndex(SCROLLED_AWAY_INDEX)
        val topBefore = titleTop("Anime $SCROLLED_AWAY_INDEX")

        //When
        screenWidthState.value = UNFOLDED_WIDTH_DP.dp
        composeRule.waitForIdle()

        //Then
        // A screen wider than the emulated window hangs past its edges, so position is what
        // tells the first visible item, not visibility.
        assertEquals(topBefore, titleTop("Anime $SCROLLED_AWAY_INDEX"))
        composeRule.onNodeWithText("Anime 0").assertDoesNotExist()
    }

    @Test
    fun theLoadingStateShowsTheSpinnerUnderTheTopBar() {
        //Given
        val spinnerDescription = runBlocking { getString(BaseRes.string.loading_in_progress) }
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        uiModelState.value = AnimeListUiModel(contentType = ContentTypeUi.LOADING)

        //When
        setScreen()

        //Then
        composeRule.onNodeWithContentDescription(spinnerDescription).assertIsDisplayed()
        composeRule.onNodeWithText(ongoingTabText).assertIsDisplayed()
        composeRule.onNodeWithText("Anime 0").assertDoesNotExist()
    }

    @Test
    fun theErrorStateShowsTheConnectionErrorImageUnderTheTopBar() {
        //Given
        val errorDescription = runBlocking { getString(CelebrityRes.string.connection_error) }
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        uiModelState.value = AnimeListUiModel(contentType = ContentTypeUi.ERROR)

        //When
        setScreen()

        //Then
        composeRule.onNodeWithContentDescription(errorDescription).assertIsDisplayed()
        composeRule.onNodeWithText(ongoingTabText).assertIsDisplayed()
        composeRule.onNodeWithText("Anime 0").assertDoesNotExist()
    }

    @Test
    fun theLoadedStateShowsTheSectionsItemsUnderTheTopBar() {
        //Given
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        uiModelState.value = loadedModel(itemCount = 2)

        //When
        setScreen()

        //Then
        composeRule.onNodeWithText("Anime 0").assertIsDisplayed()
        composeRule.onNodeWithText(ongoingTabText).assertIsDisplayed()
    }

    @Test
    fun theSectionTabsDispatchTheirOwnClickIntents() {
        //Given
        val ongoingTabText = runBlocking { getString(Res.string.on_air) }
        val announcedTabText = runBlocking { getString(Res.string.soon) }
        val searchDescription = runBlocking { getString(Res.string.search_on_description) }
        uiModelState.value = AnimeListUiModel(contentType = ContentTypeUi.LOADING)
        setScreen()

        //When
        composeRule.onNodeWithText(ongoingTabText).performClick()
        composeRule.onNodeWithText(announcedTabText).performClick()
        composeRule.onNodeWithContentDescription(searchDescription).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(
            listOf(
                AnimeListMainStore.Intent.OngoingsSectionClick,
                AnimeListMainStore.Intent.AnnouncedSectionClick,
                AnimeListMainStore.Intent.SearchSectionClick
            ),
            dispatched
        )
    }

    @Test
    fun reachingTheEndOfTheListAsksForTheNextPage() {
        //Given
        uiModelState.value = loadedModel(itemCount = LONG_LIST_ITEM_COUNT)
        setScreen()
        val countBeforeScrolling = loadNextPageCount()

        //When
        scrollListToIndex(LONG_LIST_ITEM_COUNT - 1)

        //Then
        assertEquals(0, countBeforeScrolling, "a list opened at the top has no next page to load")
        assertTrue(loadNextPageCount() >= 1, "reaching the end must ask for the next page")
    }

    @Test
    fun runningOutOfListToScrollAsksAgainSoAFailedPageCanBeRetried() {
        //Given
        // A page that failed leaves the item count and the threshold flag where they were, so
        // the threshold is never crossed again and asking only there would strand the list.
        uiModelState.value = loadedModel(itemCount = LONG_LIST_ITEM_COUNT)
        setScreen()
        scrollListToIndex(LONG_LIST_ITEM_COUNT - SHORT_SCROLL_BACK)
        val countPastTheThreshold = loadNextPageCount()

        //When
        scrollListToIndex(LONG_LIST_ITEM_COUNT - 1)

        //Then
        assertTrue(countPastTheThreshold >= 1, "passing the threshold must ask once")
        assertTrue(
            loadNextPageCount() > countPastTheThreshold,
            "reaching the bottom with nothing new must ask again"
        )
    }

    @Test
    fun sittingStillAtTheBottomAsksNoFurther() {
        //Given
        uiModelState.value = loadedModel(itemCount = LONG_LIST_ITEM_COUNT)
        setScreen()
        scrollListToIndex(LONG_LIST_ITEM_COUNT - 1)
        val countAtTheBottom = loadNextPageCount()

        //When
        // Already at the bottom: this moves nothing, so nothing new should be asked for.
        scrollListToIndex(LONG_LIST_ITEM_COUNT - 1)

        //Then
        assertEquals(countAtTheBottom, loadNextPageCount())
    }

    @Test
    fun scrollingWellShortOfTheThresholdAsksForNothing() {
        //Given
        uiModelState.value = loadedModel(itemCount = LONG_LIST_ITEM_COUNT)
        setScreen()

        //When
        scrollListToIndex(SCROLLED_AWAY_INDEX)

        //Then
        assertEquals(0, loadNextPageCount())
    }

    @Test
    fun theResetFlagScrollsBackToTheTopAndIsReportedAsHandled() {
        //Given
        uiModelState.value = loadedModel(itemCount = LONG_LIST_ITEM_COUNT)
        setScreen()
        scrollListToIndex(SCROLLED_AWAY_INDEX)
        composeRule.onNodeWithText("Anime 0").assertDoesNotExist()
        dispatched.clear()

        //When
        uiModelState.value = loadedModel(
            itemCount = LONG_LIST_ITEM_COUNT,
            isNeedToResetListPositon = true
        )
        composeRule.waitForIdle()

        //Then
        composeRule.onNodeWithText("Anime 0").assertIsDisplayed()
        assertEquals(
            listOf<AnimeListMainStore.Intent>(
                AnimeListMainStore.Intent.ChangeResetListPositionFlag(
                    isNeedToResetListPosition = false
                )
            ),
            dispatched
        )
    }
}

// Long enough that the paging threshold sits well below the last item, so the list starts out
// short of it.
private const val LONG_LIST_ITEM_COUNT = 40

// Far enough from both ends that neither the paging threshold nor the first item is in view.
private const val SCROLLED_AWAY_INDEX = 20

// A few rows back up the list — still well past the paging threshold, so the list is asked
// again rather than freshly crossing it.
private const val SHORT_SCROLL_BACK = 4

// A Pixel 4 XL, an unfolded Fold and a Pixel Tablet in landscape.
private const val PHONE_WIDTH_DP = 411
private const val UNFOLDED_WIDTH_DP = 852
private const val TABLET_LANDSCAPE_WIDTH_DP = 1280

// Tall enough for several rows of 366 dp items, so a row's neighbors are laid out at all.
private const val TALL_SCREEN_HEIGHT_DP = 1600

// Leaves the last row of a two-column grid with a single item.
private const val ODD_ITEM_COUNT = 5

// The rows ODD_ITEM_COUNT items fill at two columns.
private const val ODD_ITEM_ROW_COUNT = 3
