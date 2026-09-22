package com.alekseivinogradov.anoti.animefavorites.kmp.impl.presentation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.announced
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.episodes
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.notifications_turn_off_description
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.ongoing
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.released
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.score_image_description
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.InfoTypeUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ListItemUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.NotificationUi
import com.alekseivinogradov.anoti.animefavorites.kmp.api.presentation.model.itemcontent.ReleaseStatusUi
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.Res
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.episodes_viewed
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.episodes_viewed_minus_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.episodes_viewed_plus_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.extra_info_off_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.extra_info_on_description
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.new_episode
import com.alekseivinogradov.anoti.animefavorites.kmp.generated.resources.next_episode_short
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.formatter.DateFormatter
import com.alekseivinogradov.anoti.celebrity.kmp.impl.presentation.compose.AnotiTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.alekseivinogradov.anoti.animebase.kmp.generated.resources.Res as BaseRes

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
@RunWith(RobolectricTestRunner::class)
class AnimeFavoritesItemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private object PassThroughDateFormatter : DateFormatter {
        override fun getFormattedDate(inputText: String, fallbackText: String): String = inputText
    }

    private val baseItem = ListItemUi(
        id = 1,
        imageUrl = null,
        score = "8.42",
        infoType = InfoTypeUi.MAIN,
        name = "Frieren",
        availableEpisodesInfo = "5 / 12",
        releaseStatus = ReleaseStatusUi.ONGOING,
        notification = NotificationUi.ENABLED,
        extraEpisodesInfo = "2024-01-05",
        episodesViewed = "7",
        isNewEpisode = false
    )

    private val itemState = mutableStateOf(baseItem)

    // Null lets the item take the emulated screen's full width, whatever that is. A width has
    // to be required rather than capped: the theme's surface hands its own width down as a
    // minimum, which a plain widthIn(max) cannot go below.
    private val narrowWidthState = mutableStateOf<Dp?>(null)

    private var itemClicks = 0

    private var infoTypeClicks = 0

    private var notificationClicks = 0

    private var episodesViewedMinusClicks = 0

    private var episodesViewedPlusClicks = 0

    /** What the item's main variant shows at one particular item width. */
    private class ItemSnapshot(
        val nameShown: Boolean,
        val scoreShown: Boolean,
        val episodesShown: Boolean,
        val statusShown: Boolean,
        val notificationShown: Boolean,
        val infoTypeShown: Boolean
    )

    private fun setItem() {
        composeRule.setContent {
            AnotiTheme {
                val widthModifier = narrowWidthState.value
                    ?.let { Modifier.requiredWidth(it) }
                    ?: Modifier.fillMaxWidth()
                Box(widthModifier) {
                    AnimeFavoritesItem(
                        item = itemState.value,
                        dateFormatter = PassThroughDateFormatter,
                        onItemClick = { itemClicks++ },
                        onInfoTypeClick = { infoTypeClicks++ },
                        onNotificationClick = { notificationClicks++ },
                        onEpisodesViewedMinusClick = { episodesViewedMinusClicks++ },
                        onEpisodesViewedPlusClick = { episodesViewedPlusClicks++ }
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    // Every lookup below goes to the unmerged tree: the item's own click handler merges its
    // descendants, so the merged tree answers with the item's root instead of the part asked
    // for. The unmerged tree also holds the never-placed copy of the info panel the item
    // measures to learn its height, which this matcher filters back out.
    private val isPlaced = SemanticsMatcher("is placed") { it.layoutInfo.isPlaced }

    private fun nodeWithText(text: String): SemanticsNodeInteraction =
        composeRule.onAllNodesWithText(text, useUnmergedTree = true).filterToOne(isPlaced)

    private fun nodeWithDescription(description: String): SemanticsNodeInteraction =
        composeRule
            .onAllNodesWithContentDescription(description, useUnmergedTree = true)
            .filterToOne(isPlaced)

    private fun nodeWithTag(tag: String): SemanticsNodeInteraction =
        composeRule.onAllNodesWithTag(tag, useUnmergedTree = true).filterToOne(isPlaced)

    // The counts are joined by non-breaking spaces, so the line can only ever wrap after the
    // label.
    private fun availableEpisodesText(): String {
        val episodesLabel = runBlocking { getString(BaseRes.string.episodes) }
        return "$episodesLabel: 5\u00A0/\u00A012"
    }

    private fun showStatus(releaseStatus: ReleaseStatusUi) {
        itemState.value = baseItem.copy(releaseStatus = releaseStatus)
        composeRule.waitForIdle()
    }

    private fun shownLabels(labels: List<String>): List<String> {
        return labels.filter {
            composeRule.onAllNodesWithText(it, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun itemSnapshot(
        statusLabel: String,
        notificationDescription: String,
        infoTypeDescription: String
    ) = ItemSnapshot(
        nameShown = nodeWithText(baseItem.name).isDisplayed(),
        scoreShown = nodeWithText(baseItem.score).isDisplayed(),
        episodesShown = nodeWithText(availableEpisodesText()).isDisplayed(),
        statusShown = nodeWithText(statusLabel).isDisplayed(),
        notificationShown = nodeWithDescription(notificationDescription).isDisplayed(),
        infoTypeShown = nodeWithDescription(infoTypeDescription).isDisplayed()
    )

    // The score bar carries no semantics of its own, so its height is the span between its score
    // icon and its info-type toggle: one row while they fit side by side, two once they don't.
    private fun scoreBarHeight(infoTypeDescription: String): Dp {
        val scoreImageDescription = runBlocking {
            getString(BaseRes.string.score_image_description)
        }
        val icon = nodeWithDescription(scoreImageDescription).getUnclippedBoundsInRoot()
        val toggle = nodeWithDescription(infoTypeDescription).getUnclippedBoundsInRoot()
        return maxOf(icon.bottom, toggle.bottom) - minOf(icon.top, toggle.top)
    }

    @Test
    fun theMainVariantShowsTheNameTheScoreAndTheAvailableEpisodeCounts() {
        //Given
        itemState.value = baseItem.copy(infoType = InfoTypeUi.MAIN)

        //When
        setItem()

        //Then
        nodeWithText("Frieren").assertIsDisplayed()
        nodeWithText("8.42").assertIsDisplayed()
        nodeWithText(availableEpisodesText()).assertIsDisplayed()
    }

    @Test
    fun theExtraVariantShowsTheExtraEpisodesInfoAndTheEpisodesViewedCounter() {
        //Given
        val nextEpisodeLabel = runBlocking { getString(Res.string.next_episode_short) }
        val episodesViewedLabel = runBlocking { getString(Res.string.episodes_viewed) }
        itemState.value = baseItem.copy(infoType = InfoTypeUi.EXTRA)

        //When
        setItem()

        //Then
        nodeWithText("$nextEpisodeLabel:\n2024-01-05").assertIsDisplayed()
        nodeWithText("$episodesViewedLabel:").assertIsDisplayed()
        nodeWithText("7").assertIsDisplayed()
        nodeWithText("8.42").assertIsDisplayed()
        nodeWithText(availableEpisodesText()).assertDoesNotExist()
        nodeWithText("Frieren").assertDoesNotExist()
    }

    @Test
    fun eachReleaseStatusShowsItsOwnLabelAndAnUnknownOneShowsNone() {
        //Given
        val ongoingLabel = runBlocking { getString(BaseRes.string.ongoing) }
        val announcedLabel = runBlocking { getString(BaseRes.string.announced) }
        val releasedLabel = runBlocking { getString(BaseRes.string.released) }
        val allLabels = listOf(ongoingLabel, announcedLabel, releasedLabel)
        setItem()

        //When
        showStatus(ReleaseStatusUi.ONGOING)
        val whileOngoing = shownLabels(allLabels)
        showStatus(ReleaseStatusUi.ANNOUNCED)
        val whileAnnounced = shownLabels(allLabels)
        showStatus(ReleaseStatusUi.RELEASED)
        val whileReleased = shownLabels(allLabels)
        showStatus(ReleaseStatusUi.UNKNOWN)
        val whileUnknown = shownLabels(allLabels)

        //Then
        assertEquals(listOf(ongoingLabel), whileOngoing)
        assertEquals(listOf(announcedLabel), whileAnnounced)
        assertEquals(listOf(releasedLabel), whileReleased)
        assertEquals(emptyList(), whileUnknown)
        nodeWithText("Frieren").assertIsDisplayed()
    }

    @Test
    fun onlyAnItemWithANewEpisodeShowsTheNewEpisodeMarker() {
        //Given
        val newEpisodeMarker = runBlocking { getString(Res.string.new_episode) }
        itemState.value = baseItem.copy(isNewEpisode = false)
        setItem()
        val markerWhileNotNew = shownLabels(listOf(newEpisodeMarker))

        //When
        itemState.value = baseItem.copy(isNewEpisode = true)
        composeRule.waitForIdle()

        //Then
        assertEquals(emptyList(), markerWhileNotNew)
        nodeWithText(newEpisodeMarker).assertIsDisplayed()
    }

    @Test
    fun theNotificationToggleReportsItsClick() {
        //Given
        itemState.value = baseItem.copy(notification = NotificationUi.ENABLED)
        setItem()

        //When
        nodeWithTag(NOTIFICATION_BUTTON_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(1, notificationClicks)
        assertEquals(0, itemClicks)
        assertEquals(0, infoTypeClicks)
    }

    @Test
    fun theInfoTypeToggleReportsItsClickInBothVariants() {
        //Given
        val turnOnDescription = runBlocking { getString(Res.string.extra_info_on_description) }
        val turnOffDescription = runBlocking { getString(Res.string.extra_info_off_description) }
        itemState.value = baseItem.copy(infoType = InfoTypeUi.MAIN)
        setItem()

        //When
        nodeWithDescription(turnOnDescription).performClick()
        itemState.value = baseItem.copy(infoType = InfoTypeUi.EXTRA)
        composeRule.waitForIdle()
        nodeWithDescription(turnOffDescription).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(2, infoTypeClicks)
        assertEquals(0, itemClicks)
        assertEquals(0, notificationClicks)
    }

    @Test
    fun eachEpisodesViewedButtonReportsExactlyOneClickPerTap() {
        //Given
        val minusDescription = runBlocking {
            getString(Res.string.episodes_viewed_minus_description)
        }
        val plusDescription = runBlocking { getString(Res.string.episodes_viewed_plus_description) }
        itemState.value = baseItem.copy(infoType = InfoTypeUi.EXTRA)
        setItem()

        //When
        nodeWithDescription(minusDescription).performClick()
        nodeWithDescription(plusDescription).performClick()
        composeRule.waitForIdle()

        //Then
        // Both buttons repeat while held, so more than one click per tap would mean the repeat
        // timer had already started before the pointer went up.
        assertEquals(1, episodesViewedMinusClicks)
        assertEquals(1, episodesViewedPlusClicks)
        assertEquals(0, itemClicks)
    }

    @Test
    fun tappingTheItemBodyReportsAnItemClick() {
        //Given
        itemState.value = baseItem
        setItem()

        //When
        nodeWithTag(ITEM_TAG).performClick()
        composeRule.waitForIdle()

        //Then
        assertEquals(1, itemClicks)
        assertEquals(0, infoTypeClicks)
        assertEquals(0, notificationClicks)
    }

    @Test
    fun aWideAndANarrowItemBothRenderEveryPartOfTheMainVariant() {
        //Given
        val ongoingLabel = runBlocking { getString(BaseRes.string.ongoing) }
        val notificationDescription = runBlocking {
            getString(BaseRes.string.notifications_turn_off_description)
        }
        val infoTypeDescription = runBlocking { getString(Res.string.extra_info_on_description) }
        narrowWidthState.value = null
        setItem()

        //When
        val wide = itemSnapshot(ongoingLabel, notificationDescription, infoTypeDescription)
        val wideScoreBarHeight = scoreBarHeight(infoTypeDescription)
        narrowWidthState.value = NARROW_ITEM_WIDTH_DP.dp
        composeRule.waitForIdle()
        val narrow = itemSnapshot(ongoingLabel, notificationDescription, infoTypeDescription)
        val narrowScoreBarHeight = scoreBarHeight(infoTypeDescription)

        //Then
        listOf("wide" to wide, "narrow" to narrow).forEach { (label, snapshot) ->
            assertTrue(snapshot.nameShown, "the $label item hides its name")
            assertTrue(snapshot.scoreShown, "the $label item hides its score")
            assertTrue(snapshot.episodesShown, "the $label item hides its episode counts")
            assertTrue(snapshot.statusShown, "the $label item hides its release status")
            assertTrue(snapshot.notificationShown, "the $label item hides its notification toggle")
            assertTrue(snapshot.infoTypeShown, "the $label item hides its info-type toggle")
        }
        assertTrue(
            narrowScoreBarHeight > wideScoreBarHeight,
            "the narrow item must drop its info-type toggle onto a second line, but its score " +
                "bar stayed $narrowScoreBarHeight tall against $wideScoreBarHeight when wide"
        )
    }
}

private const val ITEM_TAG = "anime_favorites_item"

private const val NOTIFICATION_BUTTON_TAG = "notification_button"

// Narrow enough that the poster can no longer fit the score icon, the score and the info-type
// toggle on one line, which sends the score bar into its two-line arrangement.
private const val NARROW_ITEM_WIDTH_DP = 200
